import java.io.*;           // IO処理
import javax.sound.midi.*;  // Midi関連

class MyMidiPlayer
{
  private Sequencer sequencer;      // 実際にMidiを再生するプレイヤー
  private Sequence inSeq;           // 曲データ
  private Track track[];            // トラック(0~16)を保持する配列
  private boolean loadFlag = true;  // 仮保存から読み取るか否かの判定変数

  //コンストラクタ
  // Sequencerの取得・設定
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

  // 引数nameで指定したmidiファイル読み込み
  void InitReadMidi( String name ) 
  {
    try
    {
      // 音源の取り込み
      this.inSeq = MidiSystem.getSequence( new FileInputStream(name) );
      
      //トラック入手
      this.track = inSeq.getTracks();
    }
    catch( InvalidMidiDataException | IOException e )
    {
      System.out.println( "" + e );
    }
  }

  // back.mid を読み込む
  void LoadMidi()
  {
    try
    {
      // 初回以外
      if( loadFlag == false )
      {
        // 音源の取り込み
        inSeq = MidiSystem.getSequence( new FileInputStream("back.mid") );
        
        //トラック入手
        this.track = inSeq.getTracks();
      }
      // 初回はback.midがないため素通り
      else
      {
        // flag折る
        loadFlag = false;
      }
    }
    catch( InvalidMidiDataException | IOException e )
    {
      System.out.println( "" + e );
    }
  }

  // 引数Sequenceを再生する
  void play( Sequence in )
  {
    try
    {
      //音源セット
      sequencer.setSequence( in );
      
      //再生開始
      sequencer.start();
    }
    catch( InvalidMidiDataException e )
    {
      System.out.println( "" + e );
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

  //Sequencer解放
  void bye()
  {
    //停止
    this.stop();

    //クローズ
    sequencer.close();
  }

  //引数Sequenceをback.midに書き出す
  void WriteMidi( Sequence in )
  {
    try
    {
      MidiSystem.write( in, 1, new java.io.File("back.mid") );
    }
    catch( IOException e )
    {
      System.out.println( "" + e );
    }
  }

  // 音量調整
  void ChangeVelocity( int channel, int vel )
  {
    MetaMessage mMes = new MetaMessage();
    ShortMessage sMes = new ShortMessage();
    Track newTrack[] = new Track[16];
    
    // 再生停止
    long pos = sequencer.getTickPosition();
    this.stop();

    // back.midから最新状態更新
    this.LoadMidi();

    try
    {
      // 変更後保存のシーケンサ設定
      float dType = inSeq.getDivisionType();
      int dTick = inSeq.getResolution();
      Sequence outSeq = new Sequence( dType, dTick );

      // トラックの紐付け
      for( int i = 0; i < 16; i++) newTrack[ i ] = outSeq.createTrack();
        
      /* 【midiデータ編集】 */

      // テンポ設定 ( 適当 : 本来はgetした値 )
      int l = 60*1000000 / 120; // 60*1000000/tempo[micro sec/beat]
      mMes.setMessage(0x51, 
                      new byte[]{ (byte)( l /256 /256 ),
                                  (byte)( l /256 %256 ),
                                  (byte)( l %256 ) }, 
                      3);
      newTrack[ channel ].add( new MidiEvent( mMes, 0 ) );
        
      // 音色設定 
      newTrack[ channel ].add( track[ channel ].get( 189 ) );

      // midieventをぶち込む
      MidiEvent me;
      byte[] b;
      for( int i = 0; ; i++ )
      {
        try
        {
          // midiEvent 取得
          me = track[ channel ].get(i);

          // byte[]データ取得
          b = me.getMessage().getMessage();
        }
        catch( ArrayIndexOutOfBoundsException ex ) { break; }

        // ON_NOTE 
        if( ( b[0] & 0xF0 ) == 0x90 )
        {
          // velocity 変更
          sMes.setMessage( ShortMessage.NOTE_ON, channel, b[1] & 0xFF, vel & 0x007F );
          newTrack[ channel ].add( new MidiEvent( sMes, me.getTick() ) );
        }
        // NOTE_OFF
        else if( ( b[0] & 0xF0 ) == 0x80 )
        {
          newTrack[ channel ].add( me );
        }
      }
        
      //他トラック処理
      for( int i = 0; i <= track.length; i++ )
      {
        // 引数channelは書き込み済
        if( i == channel ) continue;

        try
        {
          //他トラックは上書き
          newTrack[ i ] = track[ i ];
        }
        catch( ArrayIndexOutOfBoundsException ex ) { break; }
      }
    }
    catch( InvalidMidiDataException e )
    {
      System.out.println( "" + e );
    }

    //書き出し
    this.WriteMidi( outSeq );

    // 再生再開
    sequencer.setTickPosition( pos );
    this.play( outSeq );
  }

  // 音色変更
  void ChangeVoiceMessage( int channel, int mode )
  {
    ShortMessage sMes = new ShortMessage();
    MetaMessage mMes = new MetaMessage();
    Track newTrack[] = new Track[16];
    
    //再生停止
    long pos = sequencer.getTickPosition();
    this.stop();

    //音更新
    this.LoadMidi();

    try
    {
      // 変更後保存のシーケンサ設定
      float dType = inSeq.getDivisionType();
      int dTick = inSeq.getResolution();
      Sequence outSeq = new Sequence( dType, dTick );

      // トラックの紐付け
      for( int i = 0; i < 16; i++) newTrack[ i ] = outSeq.createTrack();
        
      /* 【midiデータ編集】 */

      // テンポ設定 ( 適当 : 本来はgetした値 )
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

      //midieventをぶち込む
      MidiEvent me;
      byte[] b;
      for( int i = 0; ; i++ )
      {
        try
        {
          // midiEvent 取得
          me = track[ channel ].get(i);
          // byte[]データ取得
          b = me.getMessage().getMessage();
        }
        catch( ArrayIndexOutOfBoundsException ex ) { break; }

        // ON_NOTE と OFF_NOTE
        if( ( b[0] & 0xF0 ) == 0x90 || ( b[0] & 0xF0 ) == 0x80  )
        {
          // そのままぶち込む
          newTrack[ channel ].add( me );
        }
      }
        
      // 他トラック処理
      for( int i = 0; i <= track.length; i++ )
      {
        // channelトラックは設定済
        if( i == channel ) continue;

        try
        {
          //他トラックは上書き
          newTrack[ i ] = track[ i ];
        }
        catch( ArrayIndexOutOfBoundsException ex ) { break; }
      }
    }
    catch( InvalidMidiDataException e )
    {
      System.out.println( "" + e );
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
    // エラー処理( 頭おかしいテンポ弾く )
    if( bpm < 20 || 250 < bpm ) return;

    MetaMessage mMes = new MetaMessage();
    ShortMessage sMes = new ShortMessage();
    Track newTrack[] = new Track[16];
    
    //再生停止
    long pos = sequencer.getTickPosition();
    this.stop();

    //音更新
    this.LoadMidi();

    try
    {
      // 変更後保存のシーケンサ設定
      float dType = inSeq.getDivisionType();
      int dTick = inSeq.getResolution();
      Sequence outSeq = new Sequence( dType, dTick );

      // トラックの紐付け
      for( int i = 0; i < newTrack.length; i++) newTrack[ i ] = outSeq.createTrack();
        
      /* 【midiデータ編集】 */

      // テンポ設定
      int newL = 60*1000000 / bpm; // 60*1000000/tempo[micro sec/beat]
      mMes.setMessage(0x51, 
                      new byte[]{ (byte)( newL /256 /256 ),
                                  (byte)( newL /256 %256 ),
                                  (byte)( newL %256 ) }, 
                      3);
      
      //全トラックへテンポ・音色設定
      for( int i = 0; i < track.length; i++ )
      {
        //テンポ
        newTrack[ i ].add( new MidiEvent( mMes, 0 ) );

        //音色
        newTrack[ i ].add( track[ i ].get( 189 ) );
      }

      //midieventをぶち込む
      MidiEvent me;
      byte[] b;
      //全トラックへNOTE ON, OFF設定
      int t;
      for( t = 0; t < track.length; t++ )
      {
        for( int i = 0; ; i++ )
        {
          try
          {
            // midiEvent 取得
            me = track[ t ].get(i);
            // byte[]データ取得
            b = me.getMessage().getMessage();
          }
          catch( ArrayIndexOutOfBoundsException ex ) { break; }

          // ON_NOTE と OFF_NOTE
          if( ( b[0] & 0xF0 ) == 0x90 || ( b[0] & 0xF0 ) == 0x80  )
          {
            // そのまま
            newTrack[ t ].add( me );
          }
        }
      }
    }
    catch( InvalidMidiDataException e )
    {
      System.out.println( "" + e );
    }

    //書き出し
    this.WriteMidi( outSeq );

    // 再生再開
    sequencer.setTickPosition( pos );
    this.play( outSeq );
  }
}