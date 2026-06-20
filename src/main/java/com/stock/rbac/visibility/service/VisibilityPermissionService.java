package com.stock.rbac.visibility.service;

import java.util.List;
import java.util.Map;

public interface VisibilityPermissionService {

    List<String> selectVisibilityPermission(String userGuid, String resourceType);

    boolean checkUserVisibility(String userGuid, String resourceType, String resourceId);

    boolean checkUserFunctionPerm(String userGuid, String permCode);

    boolean checkUserRole(String userGuid, String roleCode);

    List<String> getUserRoles(String userGuid);

    List<String> getUserPermCodes(String userGuid);

    Map<String, List<String>> loadGlobalPermissionMap();

    boolean isCacheValid(long cachedVersion);
}
