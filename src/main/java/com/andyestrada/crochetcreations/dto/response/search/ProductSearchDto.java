package com.andyestrada.crochetcreations.dto.response.search;

import com.andyestrada.crochetcreations.entities.Product;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductSearchDto {

    private Long numberOfResults;
    private Long pageSize;
    private Long pageIndex;
    private List<Product> pageData;

    public Long getMaxPageIndex() {
        try {
            if (numberOfResults < pageSize) {
                return 0L;
            } else if (numberOfResults % pageSize > 0) {
                return numberOfResults / pageSize;
            } else {
                return (numberOfResults / pageSize) - 1L;
            }
        } catch (Exception e) {
            return 0L;
        }
    }

}
