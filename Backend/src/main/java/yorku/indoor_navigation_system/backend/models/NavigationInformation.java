package yorku.indoor_navigation_system.backend.models;

public class NavigationInformation {

    public String name;

    public String name2;

    public Integer floor;
    public String start;
    public String end;

    public NavigationInformation() {

    }

    public NavigationInformation(String name, Integer floor, String start, String end) {
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

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
