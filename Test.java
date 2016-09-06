import java.io.*;
import javax.sound.midi.*;

class Test{
	public static void main(String args[]) throws IOException, InterruptedException, InvalidMidiDataException
	{
		MyMidiPlayer c = new MyMidiPlayer();

		// 読み込んだら即再生開始になってる(要修正) TODO!
		c.InitReadMidi( "hoge.mid" );
		//c.ChangeBPM( 200 );
		//c.play();

		int i = 0;
		while( i < 256 )
		{
			Thread.sleep(2000);
			c.ChangeVoiceMessage( 0, i++ );
		}
		
		c.stop();

		c.bye();
	}
}