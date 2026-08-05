-- N+1 목록/상세 조회 검증용 로컬 MySQL 더미 데이터
--
-- 구성
--   * 사용자 100명
--   * 사용자별 목표 5개 (총 500개)
--   * 모든 사용자 프로필 이미지 1개
--   * 모든 목표 통계 및 대표 이미지 1개
--   * 모든 목표에 전체 더미 사용자의 약 1/3 수준으로 분산 좋아요
--   * 상세 조회 대표 목표(U1-G1)에 이미지 총 3개, 좋아요 100개, 7일 기록
--
-- 고정 이메일/제목/Object Key와 INSERT IGNORE를 사용하므로 재실행해도 중복되지 않는다.
-- 모든 더미 사용자의 로그인 비밀번호는 Seed1234! 이다.

SET NAMES utf8mb4;
START TRANSACTION;

-- BCrypt(Seed1234!)
SET @seed_password_hash = '$2y$10$0nJwoY5ss3qa8jGyTp/rQeZOfN4Z34pjQeMpxTzyIQiUREYnwkFKq';
SET @seed_user_count = 100;
SET @seed_goals_per_user = 5;

INSERT IGNORE INTO users (
    email,
    password_hash,
    nickname,
    created_at,
    updated_at,
    deleted_at
)
WITH RECURSIVE user_numbers AS (
    SELECT 1 AS user_no
    UNION ALL
    SELECT user_no + 1
    FROM user_numbers
    WHERE user_no < @seed_user_count
)
SELECT
    CONCAT('nplus1-seed-', user_no, '@example.com'),
    @seed_password_hash,
    CONCAT('n1user', user_no),
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    NULL
FROM user_numbers;

INSERT IGNORE INTO user_profile_images (
    user_id,
    object_key,
    content_type,
    file_size,
    created_at,
    updated_at
)
SELECT
    u.user_id,
    CONCAT('nplus1-seed/profiles/', u.user_id, '.jpg'),
    'image/jpeg',
    102400,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
FROM users u
WHERE u.email LIKE 'nplus1-seed-%@example.com';

-- 사용자 100명 x 목표 5개 = 총 500개
-- 생성 시각은 작성자가 교차되도록 배치해 목록 한 페이지에 서로 다른 사용자가 등장하게 한다.
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
WITH RECURSIVE
seed_users AS (
    SELECT 1 AS user_no
    UNION ALL
    SELECT user_no + 1
    FROM seed_users
    WHERE user_no < @seed_user_count
),
goal_numbers AS (
    SELECT 1 AS goal_no
    UNION ALL
    SELECT goal_no + 1
    FROM goal_numbers
    WHERE goal_no < @seed_goals_per_user
)
SELECT
    u.user_id,
    CONCAT('N+1 측정 목표 U', seed_users.user_no, '-G', goal_numbers.goal_no),
    CONCAT(
        '목록 및 상세 SQL 개수 검증용 더미 데이터. 작성자 ',
        seed_users.user_no,
        ', 목표 ',
        goal_numbers.goal_no
    ),
    DATE_ADD('2026-08-04', INTERVAL (goal_numbers.goal_no - 1) DAY),
    DATE_ADD('2026-09-04', INTERVAL (goal_numbers.goal_no - 1) DAY),
    CASE
        WHEN MOD(goal_numbers.goal_no, 5) = 0 THEN 'COMPLETED'
        ELSE 'IN_PROGRESS'
    END,
    DATE_ADD(
        '2026-08-04 09:00:00',
        INTERVAL (((goal_numbers.goal_no - 1) * @seed_user_count) + seed_users.user_no) MINUTE
    ),
    CURRENT_TIMESTAMP(6)
FROM users u
JOIN seed_users
  ON u.email = CONCAT('nplus1-seed-', seed_users.user_no, '@example.com')
CROSS JOIN goal_numbers
WHERE NOT EXISTS (
    SELECT 1
    FROM goals existing_goal
    WHERE existing_goal.user_id = u.user_id
      AND existing_goal.title = CONCAT(
          'N+1 측정 목표 U',
          seed_users.user_no,
          '-G',
          goal_numbers.goal_no
      )
);

-- 목록과 상세 응답에서 사용하는 목표 통계
INSERT IGNORE INTO goal_stats (
    goal_id,
    view_count,
    like_count,
    updated_at
)
SELECT
    g.goal_id,
    MOD(g.goal_id * 7, 100),
    0,
    CURRENT_TIMESTAMP(6)
FROM goals g
JOIN users u ON u.user_id = g.user_id
WHERE u.email LIKE 'nplus1-seed-%@example.com'
  AND g.title LIKE 'N+1 측정 목표 U%-G%';

-- 모든 목표에 대표 이미지 1개
INSERT IGNORE INTO goal_images (
    goal_id,
    object_key,
    content_type,
    file_size,
    display_order,
    created_at
)
SELECT
    g.goal_id,
    CONCAT('nplus1-seed/goals/', g.goal_id, '/cover.jpg'),
    'image/jpeg',
    204800,
    0,
    CURRENT_TIMESTAMP(6)
FROM goals g
JOIN users u ON u.user_id = g.user_id
WHERE u.email LIKE 'nplus1-seed-%@example.com'
  AND g.title LIKE 'N+1 측정 목표 U%-G%';

-- 상세 조회 대표 목표에 이미지 2개를 추가하여 총 3개 구성
INSERT IGNORE INTO goal_images (
    goal_id,
    object_key,
    content_type,
    file_size,
    display_order,
    created_at
)
SELECT
    g.goal_id,
    CONCAT('nplus1-seed/goals/', g.goal_id, '/detail-', image_numbers.display_order, '.jpg'),
    'image/jpeg',
    307200 + (image_numbers.display_order * 1024),
    image_numbers.display_order,
    CURRENT_TIMESTAMP(6)
FROM goals g
JOIN users owner ON owner.user_id = g.user_id
CROSS JOIN (
    SELECT 1 AS display_order
    UNION ALL SELECT 2
) image_numbers
WHERE owner.email = 'nplus1-seed-1@example.com'
  AND g.title = 'N+1 측정 목표 U1-G1';

-- 상세 조회 대표 목표에 모든 더미 사용자의 좋아요 추가
INSERT IGNORE INTO goal_likes (
    goal_id,
    user_id,
    created_at
)
SELECT
    detail_goal.goal_id,
    liker.user_id,
    CURRENT_TIMESTAMP(6)
FROM goals detail_goal
JOIN users owner ON owner.user_id = detail_goal.user_id
CROSS JOIN users liker
WHERE owner.email = 'nplus1-seed-1@example.com'
  AND detail_goal.title = 'N+1 측정 목표 U1-G1'
  AND liker.email LIKE 'nplus1-seed-%@example.com';

-- 목록 조회에서 좋아요 집계도 함께 측정할 수 있도록 전체 목표에 좋아요 분산
-- goal_id와 user_id 조합을 기준으로 결정하므로 재실행해도 같은 결과가 유지된다.
INSERT IGNORE INTO goal_likes (
    goal_id,
    user_id,
    created_at
)
SELECT
    g.goal_id,
    liker.user_id,
    CURRENT_TIMESTAMP(6)
FROM goals g
JOIN users owner ON owner.user_id = g.user_id
CROSS JOIN users liker
WHERE owner.email LIKE 'nplus1-seed-%@example.com'
  AND g.title LIKE 'N+1 측정 목표 U%-G%'
  AND liker.email LIKE 'nplus1-seed-%@example.com'
  AND MOD(g.goal_id + liker.user_id, 3) = 0;

-- 실제 좋아요 행 개수와 목표 통계의 like_count 동기화
UPDATE goal_stats stats
JOIN goals g ON g.goal_id = stats.goal_id
JOIN users owner ON owner.user_id = g.user_id
SET stats.like_count = (
        SELECT COUNT(*)
        FROM goal_likes likes
        WHERE likes.goal_id = g.goal_id
    ),
    stats.updated_at = CURRENT_TIMESTAMP(6)
WHERE owner.email LIKE 'nplus1-seed-%@example.com'
  AND g.title LIKE 'N+1 측정 목표 U%-G%';

-- 상세 조회 대표 목표의 7일 수행 기록
INSERT IGNORE INTO goal_logs (
    goal_id,
    log_date,
    completion_status,
    created_at,
    updated_at
)
SELECT
    g.goal_id,
    DATE_ADD('2026-08-04', INTERVAL (log_days.day_no - 1) DAY),
    CASE
        WHEN log_days.day_no IN (1, 2, 4, 5, 7) THEN 'COMPLETED'
        WHEN log_days.day_no = 3 THEN 'FAILED'
        ELSE 'SKIPPED'
    END,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
FROM goals g
JOIN users owner ON owner.user_id = g.user_id
CROSS JOIN (
    SELECT 1 AS day_no
    UNION ALL SELECT 2
    UNION ALL SELECT 3
    UNION ALL SELECT 4
    UNION ALL SELECT 5
    UNION ALL SELECT 6
    UNION ALL SELECT 7
) log_days
WHERE owner.email = 'nplus1-seed-1@example.com'
  AND g.title = 'N+1 측정 목표 U1-G1';

COMMIT;

-- 생성 결과와 상세 조회에 사용할 goal_id 확인
SELECT
    COUNT(DISTINCT u.user_id) AS seed_user_count,
    COUNT(g.goal_id) AS seed_goal_count
FROM users u
LEFT JOIN goals g
       ON g.user_id = u.user_id
      AND g.title LIKE 'N+1 측정 목표 U%-G%'
WHERE u.email LIKE 'nplus1-seed-%@example.com';

SELECT
    g.goal_id AS detail_test_goal_id,
    g.title,
    stats.view_count,
    stats.like_count,
    COUNT(DISTINCT images.goal_image_id) AS image_count,
    COUNT(DISTINCT logs.log_id) AS log_count
FROM goals g
JOIN users owner ON owner.user_id = g.user_id
JOIN goal_stats stats ON stats.goal_id = g.goal_id
LEFT JOIN goal_images images ON images.goal_id = g.goal_id
LEFT JOIN goal_logs logs ON logs.goal_id = g.goal_id
WHERE owner.email = 'nplus1-seed-1@example.com'
  AND g.title = 'N+1 측정 목표 U1-G1'
GROUP BY g.goal_id, g.title, stats.view_count, stats.like_count;
