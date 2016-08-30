import java.io.*;           //外部からデータを取り込みに必要
import javax.sound.midi.*;  //Midiの扱いに必要

class MyMidiPlayer
{
  //実際にMidiを再生するプレイヤー
  Sequencer sequencer;

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

  //コンストラクタ（実際に音を演奏するSequencerを取得・設定する。
  MyMidiPlayer( int pulse )
  {
    try
    {
      //Sequencerを取得
      // 第1引数 : 謎
      // pulse : 4部音符につき 10 
      sequencer = Sequence( Sequence.PPQ , pulse );
      
      //無限ループを設定
      sequencer.setLoopCount( Sequencer.LOOP_CONTINUOUSLY );
      
      //操作を可能にする
      sequencer.open();
    }
    //エラー処理
    catch(MidiUnavailableException e)
    {
      //エラー表示
      System.out.println( "Err=" + e );
    }
  }

  //nameで指定したmidiファイルを再生する
  void play(String name)
  {
    try
    {
      // ファイル読み込み
      FileInputStream in = new FileInputStream(name);
      // 音源の取り込み
      Sequence inSeq = MidiSystem.getSequence(in);
      //ファイルクローズ
      in.close();

      //音源セット
      sequencer.setSequence( inSeq );
      //再生開始
      sequencer.start();
    }
    //エラー処理
    catch(Exception e)
    {
      //エラー表示
      System.out.println( "Err =" + e );
    }
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
    stop();

    //クローズ
    sequencer.close();
  }

  //音量調整
  void ChangeVelocity( int vel )
  {
    //考える
  }

  //音色変更
  void ChangeVoiceMessage( int mode )
  {
    //考える
  }

  //テンポ変更
  void ChangeBPM( int bpm )
  {
    int newTick = 600000 / bpm /*/ tick (内部分解能) */;

    //考える
  }
}