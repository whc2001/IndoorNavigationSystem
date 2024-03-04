package yorku.indoor_navigation_system.backend;

public class NavigationInformation {

    public String name;
    public Integer floor;
    public Integer start;
    public Integer end;

    public NavigationInformation() {

    }

    public NavigationInformation(String name, Integer floor, Integer start, Integer end) {
        this.name = name;
        this.floor = floor;
        this.start = start;
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public Integer getFloor() {
        return floor;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }
}
