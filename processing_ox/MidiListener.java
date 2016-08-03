import javax.sound.midi.*;// for working with MIDI


public interface MidiListener {
  public void onEvent(MidiMessage message, long timeStamp);
}