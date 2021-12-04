package bgu.spl.mics.application.objects;

import bgu.spl.mics.Future;

import java.util.Collection;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU implements GPUInterface{

    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}
    enum TestResults {BAD, GOOD, NONE}


    private Type type;
    public Model model;
    Cluster cluster;

    // Memory:
    int vramCapacity;
    Collection<DataBatch> disk;
    Collection<DataBatch> trainedDisk;
    Collection<DataBatch> vRam;


    @Override
    public void insertModel(Model model) {

    }

    @Override
    public void splitToBatches(Data data) {

    }

    @Override
    public void sendData() {}

    @Override
    public void insertDbToVram(DataBatch db) {

    }

    @Override
    public void insertTrainedDbToDisk(DataBatch db) {

    }



    @Override
    public boolean isVramFull() {
        return false;
    }

    @Override
    public int getDiskSize() {
        return 0;
    }

    @Override
    public int getTrainedDiskSize() {
        return 0;
    }

    @Override
    public int getVramSize() {
        return 0;
    }

    @Override
    public DataBatch nextDataBatchDisk() {
        return null;
    }

    @Override
    public DataBatch nextDataBatchVram() {
        return null;
    }


    @Override
    public void Train() {

    }

    @Override
    public boolean testModel() {
        return false;
    }

    @Override
    public void clearGpu() {

    }


}
