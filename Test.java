import java.io.*;

class Test{
	public static void main(String args[]) throws IOException, InterruptedException
	{
		MyMidiPlayer c = new MyMidiPlayer();

		c.InitReadMidi( "hoge.mid" );

		//c.ChangeBPM( 200 );
		c.ChangeVoiceMessage( 0, 120 );

		//c.play();
		c.WriteMidi();
		//System.in.read();
		
		//c.stop();
		c.bye();
	}
}