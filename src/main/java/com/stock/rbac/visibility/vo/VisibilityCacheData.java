package com.stock.rbac.visibility.vo;

import java.io.Serializable;
import java.util.List;

public class VisibilityCacheData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long versionNo;

    private List<String> resourceIds;

    private Long createTime;

    public VisibilityCacheData() {
    }

    public VisibilityCacheData(Long versionNo, List<String> resourceIds, Long createTime) {
        this.versionNo = versionNo;
        this.resourceIds = resourceIds;
        this.createTime = createTime;
    }

    public Long getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Long versionNo) {
        this.versionNo = versionNo;
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
