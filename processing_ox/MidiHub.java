import java.util.ArrayList; // for lists of MIDI devices
import java.util.List; // for lists of MIDI devices
import java.util.Vector; // for storing listeners
import javax.sound.midi.*;// for working with MIDI

/**
 * MidiHub: a class for routing MIDI messages in real time
 * Author: Donya Quick
 * 
 * This class is intended to serve as a "middle man" between MIDI input and 
 * output devices to allow merging and splitting streams between multiple 
 * devices. 
 * 
 * @author Donya Quick
 */
public class MidiHub {
    boolean printStatus = false; // for debugging
    private MidiUnit[] inputs; // all input devices and their status settings
    private MidiUnit[] outputs; // all output devices and their status settings
    private OffReceiver offReceiver = new OffReceiver();
    private OnReceiver onReceiver; // is initialized later
    private List<MidiListener> listeners = new Vector();
   
    /**
     * Add a listener that will receive incoming MIDI messages.
     */
    public void addMidiListener(MidiListener l){
      listeners.add(l);
    }
    
    /**
     * Remove a listener so that it won't respond to incomming messages anymore.
     */
    public void removeMidiListener(MidiListener l) {
      listeners.remove(l);
    }
    
    /**
     * Default constructor that uses all available devices.
     */
    public MidiHub() {
      setDevices(MidiUtils.getInputDevices(), MidiUtils.getOutputDevices());
    }
    
    /**
     * Sets all input and output devices.
     * @param ins All input devices (to interface to MidiUtils.getInputDevices())
     * @param outs All output devices (to interface to MidiUtils.getOutputDevices())
     */
    public void setDevices(List<MidiDevice> ins, List<MidiDevice> outs) {
        inputs = new MidiUnit[ins.size()];
        outputs = new MidiUnit[outs.size()];
        
        // build MidiUnits for inputs
        for (int i=0; i<inputs.length; i++) {
            inputs[i] = new MidiUnit(ins.get(i));
        }
        
        // build MidiUnits for outputs
        for (int i=0; i<outputs.length; i++) {
            outputs[i] = new MidiUnit(outs.get(i));
        }
        
        // start the middle man "on" receiver
        onReceiver = new OnReceiver();
    }
    
    /**
     * Secondary constructor for specifying narrower lists.
     * @param ins All input devices (to interface to MidiUtils.getInputDevices())
     * @param outs All output devices (to interface to MidiUtils.getOutputDevices())
     */
    public MidiHub(List<MidiDevice> ins, List<MidiDevice> outs) {
        setDevices(ins,outs);
    }
    
    /**
     * Opens all devices.
     */
    public void openAll() {
        // open the inputs
        if(printStatus){
          System.out.println("Opening inputs...");
        }
        for (int i=0; i<inputs.length; i++) {
            try {
                // open device
                inputs[i].device.open();
                // send it to the "off" receiver by default
                inputs[i].device.getTransmitter().setReceiver(offReceiver);
                if (printStatus) {
                  System.out.println("Input device "+i+" is open.");
                }
            } catch (Exception e) {
                System.out.println("Device: "+i+": "+e.getMessage());
            }
        }
        
        // open all outputs
        if(printStatus){
          System.out.println("Opening outputs...\n");
        }
        for (int i=0; i<outputs.length; i++) {
            try {
                // open device
                outputs[i].device.open();
                if (printStatus) {
                  System.out.println("Output device "+i+" is open");
                }
            } catch (Exception e) {
                System.out.println("Device: "+i+": "+e.getMessage());
            }
        }
    }
    
    /**
     * Closes all devices.
     */
    public void closeAll() {
      if(printStatus){
        System.out.println("Closing inputs...\n");
      }
        for (int i=0; i<inputs.length; i++) {
            try {
                inputs[i].device.close();
                if (printStatus) {
                  System.out.println("Input device "+i+" closed.");
                }
            } catch (Exception e) {
                System.out.println("Device: "+i+": "+e.getMessage());
            }
        }
        
      System.out.println("Closing outputs...\n");
        for (int i=0; i<outputs.length; i++) {
            try {
                outputs[i].device.close();
                if (printStatus) {
                  System.out.println("Output device "+i+" closed.");
                }
            } catch (Exception e) {
                System.out.println("Device: "+i+": "+e.getMessage());
            }
        }
    }
    
    /**
     * Update the "on" status of a MIDI input device.
     * @param i The index of the device to update.
     * @param b Whether data should be received from the device (true = "on").
     */
    public void setInputStatus(int i, boolean b) {
        boolean oldStatus = inputs[i].status;
        if (i>=0 && i<=inputs.length) {
            inputs[i].status = b;
            try {
                updateTransmitters(inputs[i].device, b);
                if (b && !oldStatus && printStatus) {
                    System.out.println("Input #"+i+" enabled.");
                } else
                if (!b && oldStatus && printStatus) {
                    System.out.println("Input #"+i+" disabled.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Bad input device index: "+i);
        }
    }
    
    /**
     * Transmitter adjustment.
     * @param device
     * @param on 
     */
    private void updateTransmitters(MidiDevice device, boolean on) {
        //get all transmitters
        List<Transmitter> transmitters = device.getTransmitters();
        //and for each transmitter

        for(int j = 0; j<transmitters.size();j++) {
            //create a new receiver
            try {
                if(on) {
                    transmitters.get(j).setReceiver(onReceiver);
                } else {
                    transmitters.get(j).setReceiver(offReceiver);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
    /**
     * For enabling/disabling outputs.
     * @param i The index of the device.
     * @param b Whether it should receive data (true = "on").
     */
    public void setOutputStatus(int i, boolean b) {
        boolean oldStatus = outputs[i].status;
        if (i>=0 && i<=outputs.length) {
            outputs[i].status = b;
            if (b && !oldStatus && printStatus) {
                System.out.println("Output #"+i+" enabled.");
            } else
            if (!b && oldStatus && printStatus){
                System.out.println("Output #"+i+" disabled.");
            }
        } else {
            System.out.println("Bad output device index: "+i);
        }
    }
    
    /**
     * Private class for handling device/status pairs.
     */
    class MidiUnit {
        public MidiDevice device;
        public boolean status;
        public MidiUnit() {}
        public MidiUnit(MidiDevice d) {
            device = d;
            status = false;
        }
    }
    
    /**
     * "Do nothing" receiver class.
     */
    private class OffReceiver implements Receiver {
        public OffReceiver() {
            // do nothing
        }
        public void send(MidiMessage m, long timeStamp) {
            // do nothing
        }
        public void close() {
            // do nothing
        }
    }
    
    /**
     * Broadcasting receiver class.
     */
    private class OnReceiver implements Receiver  {
      public OnReceiver() {
         // do nothing
      }
       
      @Override
      public void send(MidiMessage message, long timeStamp) {
         for (int i=0; i<outputs.length; i++) {
             if (outputs[i].status) {
                try {
                    outputs[i].device.getReceiver().send(message, timeStamp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
             }
         }
         for (int i=0; i<listeners.size(); i++) {
           listeners.get(i).onEvent(message, timeStamp);
         }
      }
 
      //@Override
      public void close() {
         // do nothing - closeAll must be called manually
      }
   }
}