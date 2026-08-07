-- 검색/페이징 성능 측정용 결정적(deterministic) 더미 데이터
-- MySQL 8.0 기준
--
-- 생성 규모
--   users               : 10,000건
--   user_profile_images :  7,000건(사용자의 70%)
--   goals               : 100,000건
--   goal_stats          : 100,000건
--   goal_images         : 115,000건(목표당 0~3장)
--   goal_likes          : 약 1,100,000건(멱법칙에 가까운 계층형 분포)
--
-- 주의
--   1. 첫 DELETE는 email이 perf-search-%@example.test인 기존 성능 측정 데이터만 제거한다.
--   2. 외래키 CASCADE 때문에 재실행 시 기존 성능 측정용 이미지/likes/goals도 같이 삭제된다.
--   3. 운영 DB가 아닌 전용 성능 측정 DB에서 실행한다.
--   4. 데이터 생성은 RAND()를 사용하지 않아 재실행해도 분포가 동일하다.

SET NAMES utf8mb4;
USE amumal;

SET @perf_user_count = 10000;
SET @perf_goal_count = 100000;
SET @perf_anchor_time = TIMESTAMP('2026-08-01 12:00:00');
SET @perf_password_hash = '$2y$10$0nJwoY5ss3qa8jGyTp/rQeZOfN4Z34pjQeMpxTzyIQiUREYnwkFKq';

-- ---------------------------------------------------------------------------
-- 0. 같은 마커로 생성된 이전 측정 데이터만 정리
-- ---------------------------------------------------------------------------
DELETE FROM users
WHERE email LIKE 'perf-search-%@example.test';

-- 1부터 100,000까지의 숫자를 재귀 CTE 없이 만든다.
DROP TEMPORARY TABLE IF EXISTS perf_seed_numbers;
CREATE TEMPORARY TABLE perf_seed_numbers (
    n INT UNSIGNED NOT NULL,
    PRIMARY KEY (n)
) ENGINE = InnoDB;

INSERT INTO perf_seed_numbers (n)
SELECT
    ones.d
    + tens.d * 10
    + hundreds.d * 100
    + thousands.d * 1000
    + ten_thousands.d * 10000
    + 1 AS n
FROM (
    SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) ones
CROSS JOIN (
    SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) tens
CROSS JOIN (
    SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) hundreds
CROSS JOIN (
    SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) thousands
CROSS JOIN (
    SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) ten_thousands;

-- 목표 소유자, 검색어 버킷, 카테고리, 인기 순위를 미리 고정한다.
-- 상위 100명이 20,000개(20%)의 목표를 소유하고 나머지 사용자가 80%를 소유한다.
DROP TEMPORARY TABLE IF EXISTS perf_goal_plan;
CREATE TEMPORARY TABLE perf_goal_plan (
    seed_no INT UNSIGNED NOT NULL,
    owner_seed_no INT UNSIGNED NOT NULL,
    keyword_bucket SMALLINT UNSIGNED NOT NULL,
    category_bucket TINYINT UNSIGNED NOT NULL,
    popularity_rank INT UNSIGNED NOT NULL,
    target_like_count INT UNSIGNED NOT NULL,
    PRIMARY KEY (seed_no),
    KEY idx_perf_goal_plan_owner (owner_seed_no),
    KEY idx_perf_goal_plan_like_target (target_like_count)
) ENGINE = InnoDB;

INSERT INTO perf_goal_plan (
    seed_no,
    owner_seed_no,
    keyword_bucket,
    category_bucket,
    popularity_rank,
    target_like_count
)
SELECT
    planned.seed_no,
    planned.owner_seed_no,
    planned.keyword_bucket,
    planned.category_bucket,
    planned.popularity_rank,
    CASE
        -- 상위 0.1%: 1,000~2,999 likes
        WHEN planned.popularity_rank <= 100
            THEN 1000 + MOD(planned.seed_no * 37, 2000)
        -- 다음 0.9%: 100~499 likes
        WHEN planned.popularity_rank <= 1000
            THEN 100 + MOD(planned.seed_no * 37, 400)
        -- 다음 9%: 10~79 likes
        WHEN planned.popularity_rank <= 10000
            THEN 10 + MOD(planned.seed_no * 37, 70)
        -- 나머지 90%: 0~5 likes
        ELSE MOD(planned.seed_no * 37, 6)
    END AS target_like_count
FROM (
    SELECT
        n AS seed_no,
        CASE
            WHEN n <= 20000 THEN 1 + MOD(n - 1, 100)
            ELSE 101 + MOD((n - 20001) * 7919, 9900)
        END AS owner_seed_no,
        MOD(n - 1, 1000) AS keyword_bucket,
        MOD(n - 1, 10) AS category_bucket,
        1 + MOD((n - 1) * 7919, 100000) AS popularity_rank
    FROM perf_seed_numbers
    WHERE n <= @perf_goal_count
) planned;

-- ---------------------------------------------------------------------------
-- 1. 사용자 10,000명
-- ---------------------------------------------------------------------------
START TRANSACTION;

INSERT INTO users (
    email,
    password_hash,
    nickname,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    CONCAT('perf-search-', LPAD(n, 5, '0'), '@example.test'),
    @perf_password_hash,
    CONCAT('p', LPAD(n, 9, '0')), -- VARCHAR(10)에 정확히 들어가는 고유 닉네임
    TIMESTAMPADD(DAY, -MOD(n * 17, 1095), @perf_anchor_time),
    @perf_anchor_time,
    CASE
        WHEN MOD(n, 100) < 2
            THEN TIMESTAMPADD(DAY, -MOD(n * 13, 365), @perf_anchor_time)
        ELSE NULL
    END
FROM perf_seed_numbers
WHERE n <= @perf_user_count;

DROP TEMPORARY TABLE IF EXISTS perf_seed_users;
CREATE TEMPORARY TABLE perf_seed_users (
    seed_user_no INT UNSIGNED NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (seed_user_no),
    UNIQUE KEY uk_perf_seed_users_user_id (user_id)
) ENGINE = InnoDB;

INSERT INTO perf_seed_users (seed_user_no, user_id)
SELECT
    CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(email, '@', 1), '-', -1) AS UNSIGNED),
    user_id
FROM users
WHERE email LIKE 'perf-search-%@example.test';

-- 사용자의 70%가 프로필 이미지를 가진다.
-- 목록 조회의 user_id IN (...) 배치 쿼리에서 이미지 있음/없음이 함께 나오도록 분산한다.
INSERT INTO user_profile_images (
    user_id,
    object_key,
    content_type,
    file_size,
    created_at,
    updated_at
)
SELECT
    seeded_user.user_id,
    CONCAT(
        'perf-search/profiles/',
        LPAD(seeded_user.seed_user_no, 5, '0'),
        '.webp'
    ),
    'image/webp',
    50000 + MOD(seeded_user.seed_user_no * 7919, 450001),
    user_row.created_at,
    @perf_anchor_time
FROM perf_seed_users seeded_user
JOIN users user_row ON user_row.user_id = seeded_user.user_id
WHERE MOD(seeded_user.seed_user_no - 1, 10) < 7;

-- ---------------------------------------------------------------------------
-- 2. 목표 100,000건
--
-- keyword_bucket 분포
--   000~599: 대량공통키워드 60,000건
--     000~239 title only / 240~479 description only / 480~599 both
--   600~649: 중간빈도키워드 5,000건
--     600~619 title only / 620~639 description only / 640~649 both
--   650    : 희귀키워드 100건
--   651~999: 통제 검색어 없음 34,900건
--
-- category_bucket 분포
--   다이어트/영어/코딩/운동이 각각 title의 정확히 10%에 등장한다.
-- ---------------------------------------------------------------------------
INSERT INTO goals (
    user_id,
    title,
    description,
    start_date,
    end_date,
    status,
    created_at,
    updated_at
)
SELECT
    owner_user.user_id,
    CONCAT(
        '[PS', LPAD(plan.seed_no, 6, '0'), '] ',
        CASE plan.category_bucket
            WHEN 0 THEN '다이어트 '
            WHEN 1 THEN '영어 '
            WHEN 2 THEN '코딩 '
            WHEN 3 THEN '운동 '
            ELSE ''
        END,
        CASE MOD(plan.seed_no, 8)
            WHEN 0 THEN '아침 루틴 꾸준히 만들기'
            WHEN 1 THEN '주 3회 목표 달성하기'
            WHEN 2 THEN '매일 작은 습관 기록하기'
            WHEN 3 THEN '올해 목표 끝까지 이어가기'
            WHEN 4 THEN '새로운 기술 차근차근 배우기'
            WHEN 5 THEN '건강한 생활 습관 만들기'
            WHEN 6 THEN '한 달 챌린지 완주하기'
            ELSE '오늘보다 성장하는 계획'
        END,
        CASE
            WHEN plan.keyword_bucket < 240
              OR plan.keyword_bucket BETWEEN 480 AND 599
                THEN ' 대량공통키워드'
            WHEN plan.keyword_bucket BETWEEN 600 AND 619
              OR plan.keyword_bucket BETWEEN 640 AND 649
                THEN ' 중간빈도키워드'
            WHEN plan.keyword_bucket = 650
             AND MOD(FLOOR((plan.seed_no - 1) / 1000), 3) IN (0, 2)
                THEN ' 희귀키워드'
            ELSE ''
        END
    ) AS title,
    CASE
        -- description 검색어가 필요한 행은 NULL이 될 수 없다.
        WHEN plan.keyword_bucket BETWEEN 240 AND 599
          OR plan.keyword_bucket BETWEEN 620 AND 649
          OR (
              plan.keyword_bucket = 650
              AND MOD(FLOOR((plan.seed_no - 1) / 1000), 3) IN (1, 2)
          )
        THEN CONCAT(
            '실제 사용자가 작성한 것처럼 목표를 세운 이유와 실행 계획을 기록합니다. ',
            REPEAT(
                CASE MOD(plan.seed_no, 4)
                    WHEN 0 THEN '매일 진행 상황을 확인하고 작은 성공을 쌓겠습니다. '
                    WHEN 1 THEN '주간 단위로 결과를 돌아보고 다음 계획을 조정합니다. '
                    WHEN 2 THEN '무리하지 않고 꾸준히 실천할 수 있는 기준을 정합니다. '
                    ELSE '친구들과 성과를 공유하며 동기를 계속 유지하겠습니다. '
                END,
                CASE WHEN MOD(plan.seed_no, 10) = 7 THEN 20 ELSE 1 + MOD(plan.seed_no, 5) END
            ),
            CASE
                WHEN plan.keyword_bucket BETWEEN 240 AND 599 THEN '대량공통키워드'
                WHEN plan.keyword_bucket BETWEEN 620 AND 649 THEN '중간빈도키워드'
                ELSE '희귀키워드'
            END
        )
        -- 전체의 약 10%는 description이 NULL이다.
        WHEN MOD(plan.seed_no, 10) IN (8, 9) THEN NULL
        ELSE CONCAT(
            '목표를 시작한 배경과 구체적인 실행 방법을 기록합니다. ',
            REPEAT(
                CASE MOD(plan.seed_no, 4)
                    WHEN 0 THEN '매일 진행 상황을 확인하고 작은 성공을 쌓겠습니다. '
                    WHEN 1 THEN '주간 단위로 결과를 돌아보고 다음 계획을 조정합니다. '
                    WHEN 2 THEN '무리하지 않고 꾸준히 실천할 수 있는 기준을 정합니다. '
                    ELSE '친구들과 성과를 공유하며 동기를 계속 유지하겠습니다. '
                END,
                CASE WHEN MOD(plan.seed_no, 10) = 7 THEN 20 ELSE 1 + MOD(plan.seed_no, 5) END
            )
        )
    END AS description,
    DATE(TIMESTAMPADD(DAY, MOD(plan.seed_no * 11, 365), '2025-01-01')) AS start_date,
    CASE
        WHEN MOD(plan.seed_no, 20) < 3 THEN NULL
        ELSE DATE(
            TIMESTAMPADD(
                DAY,
                7 + MOD(plan.seed_no * 13, 180),
                TIMESTAMPADD(DAY, MOD(plan.seed_no * 11, 365), '2025-01-01')
            )
        )
    END AS end_date,
    CASE
        WHEN MOD(plan.seed_no, 20) <= 10 THEN 'IN_PROGRESS'
        WHEN MOD(plan.seed_no, 20) <= 16 THEN 'COMPLETED'
        WHEN MOD(plan.seed_no, 20) <= 18 THEN 'PAUSED'
        ELSE 'CANCELLED'
    END AS status,
    -- 50건씩 같은 생성 시각을 가져 커서의 goal_id tie-breaker도 검증할 수 있다.
    TIMESTAMPADD(
        HOUR,
        -FLOOR((@perf_goal_count - plan.seed_no) / 50) * 12,
        @perf_anchor_time
    ) AS created_at,
    @perf_anchor_time AS updated_at
FROM perf_goal_plan plan
JOIN perf_seed_users owner_user
  ON owner_user.seed_user_no = plan.owner_seed_no;

-- 생성된 실제 goal_id와 seed 번호를 연결한다.
DROP TEMPORARY TABLE IF EXISTS perf_seed_goals;
CREATE TEMPORARY TABLE perf_seed_goals (
    seed_no INT UNSIGNED NOT NULL,
    goal_id INT UNSIGNED NOT NULL,
    owner_seed_no INT UNSIGNED NOT NULL,
    target_like_count INT UNSIGNED NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (seed_no),
    UNIQUE KEY uk_perf_seed_goals_goal_id (goal_id),
    KEY idx_perf_seed_goals_like_target (target_like_count)
) ENGINE = InnoDB;

INSERT INTO perf_seed_goals (
    seed_no,
    goal_id,
    owner_seed_no,
    target_like_count,
    created_at
)
SELECT
    CAST(SUBSTRING(g.title, 4, 6) AS UNSIGNED) AS seed_no,
    g.goal_id,
    plan.owner_seed_no,
    plan.target_like_count,
    g.created_at
FROM goals g
JOIN users owner_user
  ON owner_user.user_id = g.user_id
JOIN perf_goal_plan plan
  ON plan.seed_no = CAST(SUBSTRING(g.title, 4, 6) AS UNSIGNED)
WHERE owner_user.email LIKE 'perf-search-%@example.test'
  AND LEFT(g.title, 3) = '[PS';

-- 모든 목표는 goal_stats와 1:1이다. like_count는 실제 likes 적재 후 집계한다.
INSERT INTO goal_stats (
    goal_id,
    view_count,
    like_count,
    updated_at
)
SELECT
    seeded_goal.goal_id,
    seeded_goal.target_like_count * (5 + MOD(seeded_goal.seed_no * 13, 46))
        + MOD(seeded_goal.seed_no * 97, 500) AS view_count,
    0 AS like_count,
    @perf_anchor_time
FROM perf_seed_goals seeded_goal;

-- 목표 이미지 분포
--   0장: 30,000개 목표(30%)
--   1장: 35,000개 목표(35%)
--   2장: 25,000개 목표(25%)
--   3장: 10,000개 목표(10%)
-- 총 goal_images는 115,000건이다. display_order=0이 목록 대표 이미지가 된다.
INSERT INTO goal_images (
    goal_id,
    object_key,
    content_type,
    file_size,
    display_order,
    created_at
)
SELECT
    seeded_goal.goal_id,
    CONCAT(
        'perf-search/goals/',
        LPAD(seeded_goal.seed_no, 6, '0'),
        '/',
        image_number.display_order,
        '.webp'
    ),
    'image/webp',
    100000 + MOD(
        seeded_goal.seed_no * 7919 + image_number.display_order * 104729,
        1900001
    ),
    image_number.display_order,
    TIMESTAMPADD(
        MINUTE,
        image_number.display_order + 1,
        seeded_goal.created_at
    )
FROM perf_seed_goals seeded_goal
JOIN (
    SELECT 0 AS display_order
    UNION ALL SELECT 1
    UNION ALL SELECT 2
) image_number
  ON image_number.display_order < CASE
      WHEN MOD(seeded_goal.seed_no - 1, 20) < 6 THEN 0
      WHEN MOD(seeded_goal.seed_no - 1, 20) < 13 THEN 1
      WHEN MOD(seeded_goal.seed_no - 1, 20) < 18 THEN 2
      ELSE 3
  END;

COMMIT;

-- ---------------------------------------------------------------------------
-- 3. 실제 좋아요 약 110만 건
-- ---------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS perf_like_numbers;
CREATE TEMPORARY TABLE perf_like_numbers (
    like_no INT UNSIGNED NOT NULL,
    PRIMARY KEY (like_no)
) ENGINE = InnoDB;

INSERT INTO perf_like_numbers (like_no)
SELECT n
FROM perf_seed_numbers
WHERE n <= 3000;

START TRANSACTION;

INSERT INTO goal_likes (
    goal_id,
    user_id,
    created_at
)
SELECT
    seeded_goal.goal_id,
    liker.user_id,
    TIMESTAMPADD(MINUTE, like_number.like_no, seeded_goal.created_at)
FROM perf_seed_goals seeded_goal
JOIN perf_like_numbers like_number
  ON like_number.like_no <= seeded_goal.target_like_count
JOIN perf_seed_users liker
  ON liker.seed_user_no = 1 + MOD(
      seeded_goal.owner_seed_no - 1 + like_number.like_no * 7919,
      @perf_user_count
  );

COMMIT;

-- goal_stats.like_count를 실제 goal_likes 집계 결과로 동기화한다.
START TRANSACTION;

UPDATE goal_stats stats
JOIN (
    SELECT
        likes.goal_id,
        COUNT(*) AS actual_like_count
    FROM goal_likes likes
    JOIN perf_seed_goals seeded_goal
      ON seeded_goal.goal_id = likes.goal_id
    GROUP BY likes.goal_id
) aggregated_likes
  ON aggregated_likes.goal_id = stats.goal_id
SET stats.like_count = aggregated_likes.actual_like_count,
    stats.updated_at = @perf_anchor_time;

COMMIT;

-- 대량 적재 후 옵티마이저 통계를 반드시 갱신한다.
ANALYZE TABLE
    users,
    user_profile_images,
    goals,
    goal_stats,
    goal_images,
    goal_likes;
