import java.io.*;           //外部からデータを取り込みに必要
import javax.sound.midi.*;  //Midiの扱いに必要

class MyMidiPlayer
{
  //実際にMidiを再生するプレイヤー
  Sequencer sequencer;
  //曲データ
  Sequence inSeq;
  //トラック(0~16)を保持する配列
  Track track[];
  // 仮保存から読み取るか否かの判定変数
  private boolean loadFlag = true;

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
      System.out.println( "" + e );
    }
  }

  //nameで指定したmidiファイルを再生する
  void InitReadMidi( String name ) throws IOException, InvalidMidiDataException
  {
    // 音源の取り込み
    inSeq = MidiSystem.getSequence( new FileInputStream(name) );
    
    //トラック入手
    this.track = inSeq.getTracks();

    //再生
    this.play( inSeq );
  }

  //新しくしたmidiを読み込む
  void LoadMidi() throws IOException, InvalidMidiDataException
  {
    if( loadFlag == false )
    {
      // 音源の取り込み
      inSeq = MidiSystem.getSequence( new FileInputStream("back.mid") );
      
      //トラック入手
      this.track = inSeq.getTracks();
    }
    else
    {
      // flag折る
      loadFlag = false;
    }
  }

  // 引数Sequenceを再生する
  void play( Sequence in ) throws InvalidMidiDataException
  {
    //音源セット
    sequencer.setSequence( in );
    
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
  void WriteMidi( Sequence in ) throws IOException
  {
    MidiSystem.write( in, 1, new java.io.File("back.mid"));
  }

  //音量調整
  void ChangeVelocity( int vel )
  {
    //考える
  }

  //音色変更
  void ChangeVoiceMessage( int channel, int mode ) throws IOException, InvalidMidiDataException
  {
    ShortMessage sMes = new ShortMessage();
    MetaMessage mMes = new MetaMessage();
    Track newTrack[] = new Track[16];
    
    //再生停止 : TODO
    long pos = sequencer.getTickPosition();
    this.stop();

    //音更新
    this.LoadMidi();

    // 変更後保存のシーケンサ設定
    float dType = inSeq.getDivisionType();
    int dTick = inSeq.getResolution();
    Sequence outSeq = new Sequence( dType, dTick );

    // トラックの紐付け
    for( int i = 0; i < 16; i++) newTrack[ i ] = outSeq.createTrack();
      
    // テンポ設定(適当:本来はgetした値)
    int l = 60*1000000 / 120; // 60*1000000/tempo[micro sec/beat]
    mMes.setMessage(0x51, 
                    new byte[]{ (byte)( l /256 /256 ),
                                (byte)( l /256 %256 ),
                                (byte)( l %256 ) }, 
                    3);
    newTrack[ channel ].add( new MidiEvent( mMes, 0 ) );
      
    //音色設定 
    sMes.setMessage( ShortMessage.PROGRAM_CHANGE, channel, mode, 0 );
    newTrack[ channel ].add( new MidiEvent( sMes, 0 ) );

    //音のmidieventをぶち込む
    MidiEvent e;
    byte[] m;
    for( int i = 0; ; i++ )
    {
      try
      {
        // midiEvent 取得
        e = track[ channel ].get(i);
        // byte[]データ取得
        m = e.getMessage().getMessage();
      }
      catch(ArrayIndexOutOfBoundsException ex) { break; }

      // ON_NOTE と OFF_NOTE
      if( ( m[0] & 0xF0 ) == 0x90 ||  ( m[0] & 0xF0 ) == 0x80  )
      {
        // NOTEのON, OFFはそのまま
        newTrack[ channel ].add( e );
      }
    }
      
    //他トラック処理
    for(int i = 0; i <= track.length; i++ )
    {
      //音色変えたトラックは元データで上書きしない
      if( i == channel ) continue;

      try
      {
        //他トラックは上書き
        newTrack[ i ] = track[ i ];
      }
      catch(ArrayIndexOutOfBoundsException ex) { break; }
    }

    //書き出し
    this.WriteMidi( outSeq );

    // 再生再開 : TODO
    sequencer.setTickPosition( pos );
    this.play( outSeq );
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