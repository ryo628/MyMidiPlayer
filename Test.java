import java.io.*;
import javax.sound.midi.*;

class Test{
	public static void main(String args[]) throws IOException, InterruptedException, InvalidMidiDataException
	{
		MyMidiPlayer c = new MyMidiPlayer();

		// 読み込んだら即再生開始になってる(要修正) TODO!
		c.InitReadMidi( "hoge.mid" );
		//c.play();
		int i = 0;

		
		//音量
		while( i < 100 )
		{
			Thread.sleep(2000);
			c.ChangeVelocity( 0, ( i%2 == 1 )? 50:150 );
			i++;
		}

		
		/*
		//音色変更
		while( i < 256 )
		{
			Thread.sleep(2000);
			c.ChangeVoiceMessage( 0, i++ );
		}
		*/
		
		/*
		//テンポ変更
		while( i < 100 )
		{
			Thread.sleep(4000);
			c.ChangeBPM( ( i%2 == 1 )? 60:120 );
			i-=20;
		}
		*/


		c.stop();

		c.bye();
	}
}