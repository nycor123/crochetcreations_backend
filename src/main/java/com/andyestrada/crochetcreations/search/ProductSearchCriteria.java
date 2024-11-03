package com.andyestrada.crochetcreations.search;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
@Builder
public class ProductSearchCriteria {

    private Long page;
    private Long pageSize;
    private String name;
    private String sortBy;
    private Sort.Direction sortDirection;

}
