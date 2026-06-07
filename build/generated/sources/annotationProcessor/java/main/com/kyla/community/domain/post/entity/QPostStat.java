package com.kyla.community.domain.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPostStat is a Querydsl query type for PostStat
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostStat extends EntityPathBase<PostStat> {

    private static final long serialVersionUID = 1327864376L;

    public static final QPostStat postStat = new QPostStat("postStat");

    public final NumberPath<Long> commentCount = createNumber("commentCount", Long.class);

    public final NumberPath<Long> likeCount = createNumber("likeCount", Long.class);

    public final NumberPath<Long> postId = createNumber("postId", Long.class);

    public final NumberPath<Long> viewCount = createNumber("viewCount", Long.class);

    public QPostStat(String variable) {
        super(PostStat.class, forVariable(variable));
    }

    public QPostStat(Path<? extends PostStat> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPostStat(PathMetadata metadata) {
        super(PostStat.class, metadata);
    }

}

