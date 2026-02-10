package zakir.alekperov.domain.building;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Элемент состава объекта капитального строительства.
 */
public final class BuildingComponent {

    private final int index;
    private final String name;
    private final String purpose;
    private final BigDecimal area;
    private final int floors;

    public BuildingComponent(
            int index,
            String name,
            String purpose,
            BigDecimal area,
            int floors
    ) {
        this.index = index;
        this.name = Objects.requireNonNull(name);
        this.purpose = Objects.requireNonNull(purpose);
        this.area = Objects.requireNonNull(area);
        this.floors = floors;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getPurpose() {
        return purpose;
    }

    public BigDecimal getArea() {
        return area;
    }

    public int getFloors() {
        return floors;
    }
}
