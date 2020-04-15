// ==================================================================
// This file is part of Player API.
//
// Player API is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// Player API is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License and the GNU General Public License along with Player API.
// If not, see <http://www.gnu.org/licenses/>.
// ==================================================================
package api.player.server;

import java.util.*;

public final class ServerPlayerBaseSorter
{
    public ServerPlayerBaseSorter(List<String> list, Map<String, String[]> allBaseSuperiors, Map<String, String[]> allBaseInferiors, String methodName)
    {
        this.list = list;
        this.allBaseSuperiors = allBaseSuperiors;
        this.allBaseInferiors = allBaseInferiors;
        this.methodName = methodName;
    }

    public void Sort()
    {
        if (this.list.size() <= 1) {
            return;
        }

        if (this.explicitInferiors != null) {
            this.explicitInferiors.clear();
        }

        if (this.explicitSuperiors != null) {
            this.explicitSuperiors.clear();
        }

        if (this.directInferiorsMap != null) {
            this.directInferiorsMap.clear();
        }

        if (this.allInferiors != null) {
            this.allInferiors.clear();
        }

        for (String baseId : this.list) {
            String[] inferiorNames = this.allBaseInferiors.get(baseId);
            boolean hasInferiors = inferiorNames != null && inferiorNames.length > 0;

            String[] superiorNames = this.allBaseSuperiors.get(baseId);
            boolean hasSuperiors = superiorNames != null && superiorNames.length > 0;

            if ((hasInferiors || hasSuperiors) && this.directInferiorsMap == null) {
                this.directInferiorsMap = new Hashtable<>();
            }

            if (hasInferiors) {
                this.explicitInferiors = build(baseId, this.explicitInferiors, this.directInferiorsMap, null, inferiorNames);
            }

            if (hasSuperiors) {
                this.explicitSuperiors = build(baseId, this.explicitSuperiors, null, this.directInferiorsMap, superiorNames);
            }
        }
        if (this.directInferiorsMap != null) {
            for (int i = 0; i < this.list.size() - 1; i++) {
                for (int n = i + 1; n < this.list.size(); n++) {
                    String left = this.list.get(i);
                    String right = this.list.get(n);

                    Set<String> leftInferiors = null, rightInferiors = null;
                    if (this.explicitInferiors != null) {
                        leftInferiors = this.explicitInferiors.get(left);
                        rightInferiors = this.explicitInferiors.get(right);
                    }

                    Set<String> leftSuperiors = null, rightSuperiors = null;
                    if (this.explicitSuperiors != null) {
                        leftSuperiors = this.explicitSuperiors.get(left);
                        rightSuperiors = this.explicitSuperiors.get(right);
                    }

                    boolean leftWantsToBeInferiorToRight = leftSuperiors != null && leftSuperiors.contains(right);
                    boolean leftWantsToBeSuperiorToRight = leftInferiors != null && leftInferiors.contains(right);

                    boolean rightWantsToBeInferiorToLeft = rightSuperiors != null && rightSuperiors.contains(left);
                    boolean rightWantsToBeSuperiorToLeft = rightInferiors != null && rightInferiors.contains(left);

                    if (leftWantsToBeInferiorToRight && rightWantsToBeInferiorToLeft) {
                        throw new UnsupportedOperationException("Can not sort ServerPlayerBase classes for method '" + this.methodName + "'. '" + left + "' wants to be inferior to '" + right + "' and '" + right + "' wants to be inferior to '" + left + "'");
                    }
                    if (leftWantsToBeSuperiorToRight && rightWantsToBeSuperiorToLeft) {
                        throw new UnsupportedOperationException("Can not sort ServerPlayerBase classes for method '" + this.methodName + "'. '" + left + "' wants to be superior to '" + right + "' and '" + right + "' wants to be superior to '" + left + "'");
                    }

                    if (leftWantsToBeInferiorToRight && leftWantsToBeSuperiorToRight) {
                        throw new UnsupportedOperationException("Can not sort ServerPlayerBase classes for method '" + this.methodName + "'. '" + left + "' wants to be superior and inferior to '" + right + "'");
                    }
                    if (rightWantsToBeInferiorToLeft && rightWantsToBeSuperiorToLeft) {
                        throw new UnsupportedOperationException("Can not sort ServerPlayerBase classes for method '" + this.methodName + "'. '" + right + "' wants to be superior and inferior to '" + left + "'");
                    }
                }
            }

            if (this.allInferiors == null) {
                this.allInferiors = new Hashtable<>();
            }

            for (String s : this.list) {
                this.build(s, null);
            }
        }

        if (this.withoutSuperiors == null) {
            this.withoutSuperiors = new LinkedList<>();
        }

        int offset = 0;
        int size = this.list.size();

        while (size > 1) {
            this.withoutSuperiors.clear();
            for (int i = offset; i < offset + size; i++) {
                this.withoutSuperiors.add(this.list.get(i));
            }

            if (this.allInferiors != null) {
                for (int i = offset; i < offset + size; i++) {
                    Set<String> inferiors = this.allInferiors.get(this.list.get(i));
                    if (inferiors != null) {
                        this.withoutSuperiors.removeAll(inferiors);
                    }
                }
            }

            boolean initial = true;
            for (int i = offset; i < offset + size; i++) {
                String key = this.list.get(i);
                if (this.withoutSuperiors.contains(key)) {
                    if (initial) {
                        Set<String> inferiors = null;
                        if (this.allInferiors != null) {
                            inferiors = this.allInferiors.get(key);
                        }
                        if (inferiors == null || inferiors.isEmpty()) {
                            this.withoutSuperiors.remove(key);
                            size--;
                            offset++;
                            continue;
                        }
                    }
                    this.list.remove(i--);
                    size--;
                }
                initial = false;
            }
            this.list.addAll(offset + size, this.withoutSuperiors);
        }
    }

    private Set<String> build(String type, String startType)
    {
        Set<String> inferiors = this.allInferiors.get(type);
        if (inferiors == null) {
            inferiors = this.build(type, null, startType != null ? startType : type);
            if (inferiors == null) {
                inferiors = Empty;
            }
            this.allInferiors.put(type, inferiors);
        }
        return inferiors;
    }

    private Set<String> build(String type, Set<String> inferiors, String startType)
    {
        Set<String> directInferiors = this.directInferiorsMap.get(type);
        if (directInferiors == null) {
            return inferiors;
        }

        if (inferiors == null) {
            inferiors = new HashSet<>();
        }

        for (String inferiorType : directInferiors) {
            if (inferiorType.equals(startType)) {
                throw new UnsupportedOperationException("Can not sort ServerPlayerBase classes for method '" + this.methodName + "'. Circular superiority found including '" + startType + "'");
            }
            if (this.list.contains(inferiorType)) {
                inferiors.add(inferiorType);
            }

            Set<String> inferiorSet;
            try {
                inferiorSet = this.build(inferiorType, startType);
            } catch (UnsupportedOperationException uoe) {
                throw new UnsupportedOperationException("Can not sort ServerPlayerBase classes for method '" + this.methodName + "'. Circular superiority found including '" + inferiorType + "'", uoe);
            }

            if (inferiorSet != Empty) {
                inferiors.addAll(inferiorSet);
            }
        }
        return inferiors;
    }

    private static Map<String, Set<String>> build(String baseId, Map<String, Set<String>> map, Map<String, Set<String>> directMap, Map<String, Set<String>> otherDirectMap, String[] names)
    {
        if (map == null) {
            map = new Hashtable<>();
        }

        Set<String> types = new HashSet<>();
        for (String name : names) {
            if (name != null) {
                types.add(name);
            }
        }

        if (directMap != null) {
            getOrCreateSet(directMap, baseId).addAll(types);
        }

        if (otherDirectMap != null) {
            for (String type : types) {
                getOrCreateSet(otherDirectMap, type).add(baseId);
            }
        }

        map.put(baseId, types);
        return map;
    }

    private static Set<String> getOrCreateSet(Map<String, Set<String>> map, String key)
    {
        Set<String> value = map.get(key);
        if (value != null) {
            return value;
        }

        value = new HashSet<>();
        map.put(key, value);
        return value;
    }

    private Map<String, Set<String>> explicitInferiors;
    private Map<String, Set<String>> explicitSuperiors;
    private Map<String, Set<String>> directInferiorsMap;
    private Map<String, Set<String>> allInferiors;
    private List<String> withoutSuperiors;
    private final List<String> list;
    private final Map<String, String[]> allBaseSuperiors;
    private final Map<String, String[]> allBaseInferiors;
    private final String methodName;
    private static final Set<String> Empty = new HashSet<>();
}
