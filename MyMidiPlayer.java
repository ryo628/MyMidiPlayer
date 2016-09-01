import java.io.*;           //外部からデータを取り込みに必要
import javax.sound.midi.*;  //Midiの扱いに必要

class MyMidiPlayer
{
  //実際にMidiを再生するプレイヤー
  Sequencer sequencer;
  Sequence inSeq;
  Track track[];

  //コンストラクタ（実際に音を演奏するSequencerを取得・設定する。
  MyMidiPlayer()
  {
    try
    {
      //Sequencerを取得
      sequencer = MidiSystem.getSequencer();
      
      //無限ループを設定
      sequencer.setLoopCount( Sequencer.LOOP_CONTINUOUSLY );
      
      //操作を可能にする
      sequencer.open();
    }
    //エラー処理
    catch(MidiUnavailableException e)
    {
      //エラー表示
      System.out.println("Err="+e);
    }
  }

  //nameで指定したmidiファイルを再生する
  void ReadMidi(String name)
  {
    try
    {
      // ファイル読み込み
      FileInputStream in = new FileInputStream(name);
      // 音源の取り込み
      inSeq = MidiSystem.getSequence(in);
      //ファイルクローズ
      in.close();

      //トラック入手
      this.track = inSeq.getTracks();
    }
    //エラー処理
    catch(Exception e)
    {
      //エラー表示
      System.out.println( "Err =" + e );
    }
  }

  //nameで指定したmidiファイルを再生する
  void play()
  {
    try
    {
      //音源セット
      sequencer.setSequence( inSeq );
    }
    catch( InvalidMidiDataException e )
    {
      System.out.println( "Err = " + e );
    }

    //再生開始
    sequencer.start();
  }

 //再生を停止する。
  void stop()
  {
    //再生中なら
    if( sequencer.isRunning() )
    {
      //停止
      sequencer.stop();
    }
  }

  //シーケンサー解放
  void bye()
  {
    //停止
    this.stop();

    //クローズ
    sequencer.close();
  }

  //音量調整
  void ChangeVelocity( int vel )
  {
    //考える
  }

  //音色変更
  void ChangeVoiceMessage( int channel, int mode )
  {
    ShortMessage sMes = new ShortMessage();

    //
    try
    {
      sMes.setMessage( ShortMessage.PROGRAM_CHANGE, channel, mode, 0 );
      track[ channel ].remove( track[channel].get(ShortMessage.PROGRAM_CHANGE) );
      track[ channel ].add( new MidiEvent( sMes, 0 ) );
      System.out.println("voiceMessage is changed!");
    }
    catch( InvalidMidiDataException e )
    {
      System.out.println( "Err = " + e );
    }
  }

  //テンポ変更
  void ChangeBPM( int bpm )
  {
    //エラー処理(適当)
    if( bpm < 20 || 256 > bpm ) return;

    //4分音符の長さ[micro s]
    int newL = 60 * 1000000 / bpm;

    sequencer.setTempoInBPM( (float)bpm );

    /*
    try
    {
      //新しいテンポのMetaMessage作成
      MetaMessage mMes = new MetaMessage();
      mMes.setMessage( 0x51, //テンポ
                       new byte[]{ (byte)( newL /256 /256 ),
                                   (byte)( newL /256 ),
                                   (byte)( newL %256 ),},
                       3);

      MetaMessage rm = new MetaMessage();
      rm.setMessage( 0x51, new byte[3], 3 );

      //全トラックへの適用
      for( Track t : track )
      {
        //
        t.remove( new MidiEvent( rm, 0 ) );
        //新テンポ
        boolean flag = t.add( new MidiEvent( mMes, 0 ) );

        System.out.println("re tempo" + flag );
      }
    }
    catch( InvalidMidiDataException e )
    {
      //
      System.out.println( "Err =" + e );
    }*/
  }
}