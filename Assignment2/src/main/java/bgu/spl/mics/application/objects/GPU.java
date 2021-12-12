package bgu.spl.mics.application.objects;

import bgu.spl.mics.Future;
import bgu.spl.mics.application.services.GPUTimeService;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

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
    private GPUTimeService gpuTimeService;

    // Memory:
    int vramCapacity;
    LinkedBlockingQueue<DataBatch> disk;
    LinkedBlockingQueue<DataBatch> trainedDisk;
    LinkedBlockingQueue<DataBatch> vRam;


    @Override
    public void insertModel(Model model) {
        if (disk.size() == 0 && model.status != Model.statusEnum.Training){
            this.model = model;
        }
    }

    @Override
    public void splitToBatches(Data data) {
        int numberOfBatches = data.getSize()/1000;
        for (int i = 0; i<numberOfBatches;i++){
            //TODO consult with Noyhoz
            if (i<=vramCapacity){
                cluster.insertUnProcessedData(new DataBatch(data));
            }
            disk.add(new DataBatch(data));
        }
    }

    @Override
    public void sendData() {
        if (disk.size()>0){
            cluster.insertUnProcessedData(disk.poll());
        }
    }

    @Override
    public void insertDbToVram(DataBatch db) {
        if (db.isProcessed()){
           try{
               vRam.put(db);
           }catch(Exception ignored){

           }
        }
    }

    @Override
    public void insertTrainedDbToDisk(DataBatch db) {
        if (db.isTrained()){
            trainedDisk.add(db);
        }
    }



    @Override
    public boolean isVramFull() {
        return getVramSize() == vramCapacity;
    }

    @Override
    public int getDiskSize() {
        return disk.size();
    }

    @Override
    public int getTrainedDiskSize() {
        return trainedDisk.size();
    }

    @Override
    public int getVramSize() {
        return vRam.size();
    }

    @Override
    public DataBatch nextDataBatchDisk() {
        return disk.peek();
    }

    @Override
    public DataBatch nextDataBatchVram() {
        return vRam.peek();
    }


    @Override
    public void Train() {
        if (!vRam.isEmpty()){
            DataBatch db = vRam.poll();
            int start = gpuTimeService.getTime();
            int trainTime = calculateTrianTime();
            while(gpuTimeService.getTime() - start < trainTime){}
            db.setTrained(true);
            insertTrainedDbToDisk(db);
        }
    }

    @Override
    public boolean testModel() {
        Random random = new Random();
        int num = 1 + random.nextInt(10);
        if (model.getStudent().getDegree() == Student.Degree.MSc && num <= 6){
            return true;
        }else if (model.getStudent().getDegree() == Student.Degree.PhD && num <= 8){
            return true;
        }
        return false;
    }

    @Override
    public void clearGpu() {
        disk.clear();
        trainedDisk.clear();
        vRam.clear();
    }

    public int calculateTrianTime(){
        if (type == Type.RTX3090){
            return 1;
        }else if(type == Type.RTX2080){
            return 2;
        }else{
            return 4;
        }
    }

}
