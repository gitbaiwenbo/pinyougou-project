package com.itcast.core.service.search;

import java.util.Map;

public interface ItemSearchService {
    public Map<String,Object> search(Map<String,String> specMap);

    public void addItemToSolr(long id);
    public void deleteItemFromSolr(Long id);
}
