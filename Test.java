class Test{
	public static void main(String args[]) throws IOException, InterruptedException, InvalidMidiDataException
	{
		MyMidiPlayer c = new MyMidiPlayer();
		int i = 0;

		//読み込み
		c.InitReadMidi( "hoge.mid" );
		//再生
		c.play();

		/* 動作確認 */

		//音量
		while( i < 100 )
		{
			Thread.sleep(2000);
			c.ChangeVelocity( 0, ( i++%2 == 1 )? 50:150 );
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
			c.ChangeBPM( ( i++%2 == 1 )? 60:120 );
		}
		*/

		//再生停止
		c.stop();
		//シーケンサ開放処理
		c.bye();
	}
}