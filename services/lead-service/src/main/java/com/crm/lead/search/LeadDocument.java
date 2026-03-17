package com.crm.lead.search;

import com.crm.lead.enums.Industry;
import com.crm.lead.enums.LeadSource;
import com.crm.lead.enums.LeadStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;

@Document(indexName = "leads")
@Setting(shards = 1, replicas = 0)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeadDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Keyword)
    private String phone;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String company;

    @Field(type = FieldType.Keyword)
    private Industry industry;

    @Field(type = FieldType.Keyword)
    private LeadSource source;

    @Field(type = FieldType.Keyword)
    private LeadStatus status;

    @Field(type = FieldType.Long)
    private Long assignedUserId;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;
}
