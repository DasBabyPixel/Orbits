package orbits.data.level;

public interface Transaction {
    void execute();

    void undo();
}
