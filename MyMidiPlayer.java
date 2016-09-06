import java.io.*;           //外部からデータを取り込みに必要
import javax.sound.midi.*;  //Midiの扱いに必要

class MyMidiPlayer
{
  //実際にMidiを再生するプレイヤー
  Sequencer sequencer;
  //曲データ
  Sequence inSeq;
  Sequence outSeq;
  //トラック(0~16)を保持する配列
  Track track[];
  Track newTrack[] = new Track[16];

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
  void InitReadMidi(String name)
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

  //新しくしたmidiを読み込む
  void LoadMidi()
  {
    try
    {
      // ファイル読み込み
      FileInputStream in = new FileInputStream("back.mid");
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
      sequencer.setSequence( outSeq );
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

  //変更後midi書き出し
  void WriteMidi()
  {
    try
    {
      MidiSystem.write( outSeq, 1, new java.io.File("back.mid"));
    }
    catch( IOException e )
    {
      System.out.println(""+ e);
    }
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
    MidiEvent e;
    byte[] m;
    MetaMessage mmessage = new MetaMessage();

    //
    try
    {
      // 変更後保存のシーケンサ設定
      outSeq = new Sequence(Sequence.PPQ, 480);
      // トラックの紐付け
      for( int i = 0; i < 16; i++) newTrack[ i ] = outSeq.createTrack();
      
      // テンポ設定(適当:本来はgetした値)
      mmessage.setMessage(0x51, 
                          new byte[]{ (byte)( 60*1000000/60 /256 /256 ),
                                      (byte)( 60*1000000/60 /256 %256 ),
                                      (byte)( 60*1000000/60 %256 ) }, 
                          3);
      newTrack[ channel ].add(new MidiEvent(mmessage, 0));
      
      //音色設定 
      sMes.setMessage( ShortMessage.PROGRAM_CHANGE, channel, mode, 0 );
      newTrack[ channel ].add( new MidiEvent( sMes, 0 ) );

      //音のmidieventをぶち込む
      for( int i = 0; ; )
      {
          try
          {
            e = track[ channel ].get(i++);
            m = e.getMessage().getMessage();
          }
          catch(ArrayIndexOutOfBoundsException ex)
          {
            break;
          }

          // ON_NOTE と OFF_NOTE
          if( ( m[0] & 0xF0 ) == 0x90 ||  ( m[0] & 0xF0 ) == 0x80  )
          {
            newTrack[ channel ].add( e );
          }
      }
      
      //変更
      //outSeq.addTrack( newTrack[ channel ] );

      //他トラックはそのまま
      for(int i = 0; i <= track.length; i++ )
      {
        if( i == channel ) continue;

        try
          {
            newTrack[ i ] = track[ i ];
          }
          catch(ArrayIndexOutOfBoundsException ex)
          {
            break;
          }
      }
    }
    catch( InvalidMidiDataException me )
    {
      System.out.println( "Err = " + me );
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