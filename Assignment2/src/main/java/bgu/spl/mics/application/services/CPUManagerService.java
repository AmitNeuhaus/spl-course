package bgu.spl.mics.application.services;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.CPU;

public class CPUManagerService extends MicroService {

    private CPU cpu;
    boolean terminated;
    public CPUManagerService(CPU cpu){
        super("CPUManagerService");
        this.cpu = cpu;
        terminated = false;
    }

    @Override
    protected void initialize() {
        while (!terminated) {
            try {
                cpu.process();
            } catch (Exception e){
                terminated = true;
                System.out.println("Terminated CPU MANAGER");
                terminate();
            }
        }
    }
}
