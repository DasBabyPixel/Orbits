package orbits.data.level;

import gamelauncher.engine.util.collections.Collections;

import java.util.Deque;

public class TransactionTracker {
    private final Deque<Transaction> transactions = Collections.newConcurrentDeque();
    private final Deque<Transaction> redoQueue = Collections.newConcurrentDeque();

    /**
     * Adds and executes a transaction
     */
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        redoQueue.clear();
        transaction.execute();
    }

    public boolean canRedo() {
        return !redoQueue.isEmpty();
    }

    public void redo() {
        Transaction last = redoQueue.pollLast();
        if (last != null) {
            transactions.add(last);
            last.execute();
        }
    }

    public boolean canUndo() {
        return !transactions.isEmpty();
    }

    public void undo() {
        Transaction last = transactions.pollLast();
        if (last != null) {
            last.undo();
            redoQueue.add(last);
        }
    }
}
