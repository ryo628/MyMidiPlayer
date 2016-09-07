import java.io.*;           //外部からデータを取り込みに必要
import javax.sound.midi.*;  //Midiの扱いに必要
import java.nio.ByteBuffer;

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
  void ChangeVelocity( int channel, int vel ) throws IOException, InvalidMidiDataException
  {
    MetaMessage mMes = new MetaMessage();
    ShortMessage sMes = new ShortMessage();
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
    newTrack[ channel ].add( track[ channel ].get( 189 ) );

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

      // ON_NOTE 
      if( ( m[0] & 0xF0 ) == 0x90 )
      {
        // velocity 変更
        sMes.setMessage( ShortMessage.NOTE_ON, channel, m[1] & 0xFF, vel & 0x007F );
        newTrack[ channel ].add( new MidiEvent( sMes, e.getTick() ) );
      }
      // NOTE_OFF
      else if( ( m[0] & 0xF0 ) == 0x80 )
      {
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
  void ChangeBPM( int bpm ) throws IOException, InvalidMidiDataException
  {
    //エラー処理(適当)
    if( bpm < 20 || 400 < bpm ) return;

    MetaMessage mMes = new MetaMessage();
    ShortMessage sMes = new ShortMessage();
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
    for( int i = 0; i < newTrack.length; i++) newTrack[ i ] = outSeq.createTrack();
      
    // テンポ設定
    int l = 60*1000000 / bpm; // 60*1000000/tempo[micro sec/beat]
    mMes.setMessage(0x51, 
                    new byte[]{ (byte)( l /256 /256 ),
                                (byte)( l /256 %256 ),
                                (byte)( l %256 ) }, 
                    3);
    
    //全トラックへテンポ・音色設定
    for( int i = 0; i < track.length; i++ )
    {
      //テンポ
      newTrack[ i ].add( new MidiEvent( mMes, 0 ) );

      //音色
      //sMes.setMessage( ShortMessage.PROGRAM_CHANGE, i, 0, 0 );
      newTrack[ i ].add( track[ i ].get( 189 ) );
    }

    //音のmidieventをぶち込む
    MidiEvent e;
    byte[] m;

    //全トラックへNOTE ON, OFF設定
    int t;
    for( t = 0; t < track.length; t++ )
    {
      for( int i = 0; ; i++ )
      {
        try
        {
          // midiEvent 取得
          e = track[ t ].get(i);
          // byte[]データ取得
          m = e.getMessage().getMessage();
        }
        catch(ArrayIndexOutOfBoundsException ex) { break; }

        // ON_NOTE と OFF_NOTE
        if( ( m[0] & 0xF0 ) == 0x90 ||  ( m[0] & 0xF0 ) == 0x80  )
        {
          // NOTEのON, OFFはそのまま
          newTrack[ t ].add( e );
        }
      }
    }

    //他トラック処理
    for(int i = t; i <= track.length; i++ )
    {
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
}