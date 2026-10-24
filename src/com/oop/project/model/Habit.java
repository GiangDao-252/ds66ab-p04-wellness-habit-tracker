import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Habit {
    private String id;
    private String name;
    private Frequency frequency;
    private double targetValue;
    private String unit;
    private LocalDate startDate;
    private LocalDate endDate;
    private final List<CheckIn> checkIns; // Composition

    public Habit(String id, String name, Frequency frequency, double targetValue, 
                 String unit, LocalDate startDate, LocalDate endDate) {
        if (targetValue <= 0) {
            throw new IllegalArgumentException("Target value must be greater than zero");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        this.id = id;
        this.name = name;
        this.frequency = frequency;
        this.targetValue = targetValue;
        this.unit = unit;
        this.startDate = startDate;
        this.endDate = endDate;
        this.checkIns = new ArrayList<>();
    }

    // Managing CheckIns (Composition logic)
    public void addCheckIn(LocalDate date, double value) {
        CheckIn checkIn = new CheckIn(date, value);
        this.checkIns.add(checkIn);
    }

    public boolean removeCheckIn(LocalDate date) {
        return this.checkIns.removeIf(c -> c.getDate().equals(date));
    }

    // Encapsulation: Return unmodifiable list
    public List<CheckIn> getCheckIns() {
        return Collections.unmodifiableList(checkIns);
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public Frequency getFrequency() { return frequency; }
    public double getTargetValue() { return targetValue; }
    public String getUnit() { return unit; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}