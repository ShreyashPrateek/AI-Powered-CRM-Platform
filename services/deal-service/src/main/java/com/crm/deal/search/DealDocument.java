package com.crm.deal.search;

import com.crm.deal.enums.DealStage;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Document(indexName = "deals")
@Setting(shards = 1, replicas = 0)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DealDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Long)
    private Long leadId;

    @Field(type = FieldType.Double)
    private BigDecimal value;

    @Field(type = FieldType.Keyword)
    private DealStage stage;

    @Field(type = FieldType.Integer)
    private Integer probability;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate expectedCloseDate;

    @Field(type = FieldType.Long)
    private Long ownerId;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;
}
