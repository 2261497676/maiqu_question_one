package com.cfl.common;

import lombok.Data;

@Data
public class PageRequest {

    protected int pageSize = 10;

    protected int pageNum = 1;

}
