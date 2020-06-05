package org.xusheng.ioliw.javasample;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WorkingWithResources {
    public static void main(String[] args) throws IOException {
        FileWriterEAM.use("test.txt", writer -> writer.writeStuff("hello world!"));
    }
}

@FunctionalInterface
interface UseInstance<T, X extends Throwable> {
    void accept(T instance) throws X;
}

class FileWriterEAM {
    public static void use(
        final String fileName,
        final UseInstance<FileWriterEAM, IOException> block
    ) throws IOException {
        final FileWriterEAM writerEAM = new FileWriterEAM(fileName);
        try {
            block.accept(writerEAM);
        } finally {
            writerEAM.close();
        }
    }

    private final FileWriter writer;
    private FileWriterEAM(final String fileName) throws IOException {
        writer = new FileWriter(fileName);
    }
    private void close() throws IOException {
        System.out.println("close called automatically...");
        writer.close(); }
    public void writeStuff(final String message) throws IOException {
        writer.write(message);
    }
}

class Locker {
    private Lock lock = new ReentrantLock();

    public void doOp1() {
        runLocked(lock, () -> {/*...critical code ... */});
    }

    public static void runLocked(Lock lock, Runnable block) {
        lock.lock();
        try {
            block.run();
        } finally {
            lock.unlock();
        }
    }
}
