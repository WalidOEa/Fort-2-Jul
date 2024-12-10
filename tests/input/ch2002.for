      Program Map01
C
C
C     This program uses the UNIRAS software
C     on the VAXen to generate maps of the world
C     with symbols for the Tsunamis organised by
C     region. There are 10 regions, and the idea
C     is to plot all tsunamis in each region with
C     a different symbol.
C
C
      LOGICAL TRIAL,SCREEN
      REAL LONG,LAT
C
C
C     Switch on diagnostics
C
C
      SCREEN=.FALSE.
      TRIAL=.FALSE.
C      PRINT *,' Testing on/off'
C      READ (UNIT=*, FMT=*, END=9,ERR=9)TRIAL
C9     CONTINUE
C
C
C     Read in the tsunami data
C
C
      CALL DATAIN(TRIAL)
C
C
C     I now have all the tsunami data latitude and longitude
C     values read in to the arrays in the TSUNAM common block.
C
C      Prompt the user for what kind of
C      map resolution they want. There are five options
C
C      Points        Routine
C
C      119,650       WEXTEND
C       75,500       WRED1
C       43,100       WRED2
C       19,300       WRED3
C        4,420       WRED4
C
C
      PRINT *,' What resolution map do you want'
      PRINT *
      PRINT *,' 1 = 119,650'
      PRINT *,' 2 =  75,500'
      PRINT *,' 3 =  43,100'
      PRINT *,' 4 =  19,300'
      PRINT *,' 5 =   4,420'
      PRINT *
100   READ (UNIT=*,FMT=*,END=200,ERR=200) IRES
200   IF ( (IRES.LT.1) .OR. (IRES.GT. 5) ) THEN
         PRINT *,' Please input a number in the range 1 to 5'
         GOTO 100
      ENDIF
C
C
C     The next thing to do is sort out what kind of
C     map projection the user wants. I am offering
C     one of the following
C
C     Projection
C
C     Lambert       - equal area        -  rectangle
C     Mercator      - equal direction   -  rectangle
C     Hammer        - equal area        -  oval
C     Bonne         -                   -  heart
C     Orthographic  - globe             -  round
C
C
      PRINT *,' What projection would you like?'
      PRINT *
      PRINT *,' 1 = Lambert       - equal area        -  rectangle'
      PRINT *,' 2 = Mercator      - equal direction   -  rectangle'
      PRINT *,' 3 = Hammer        - equal area        -  oval'
      PRINT *,' 4 = Bonne         -                   -  heart'
      PRINT *,' 5 = Orthographic  - globe             -  round'
300   READ (UNIT=*,FMT=*,END=400,ERR=400) IPROJ
400   IF ((IPROJ.LT.1) .OR. (IPROJ.GT. 5)) THEN
         PRINT *,' Please input a number in the range 1 to 5'
         GOTO 300
      ENDIF
C
C
C     The next thing to do is set the centre of the map
C     In this case the Pacific
C
      LAT=0.0
      LONG=180.0
C
C
C     I offer the user a choice of region
C     to plot.
C
C
      PRINT *,' Which region do you wish to plot?'
      PRINT *,' 0 = all regions'
      PRINT *,' 1 = Hawaii'
      PRINT *,' 2 = New Zealand and South Pacific Islands'
      PRINT *,' 3 = Papua New Guinea and Solomon Islands'
      PRINT *,' 4 = Indonesia'
      PRINT *,' 5 = Philippines'
      PRINT *,' 6 = Japan'
      PRINT *,' 7 = Kuril Islands and Kamchatka'
      PRINT *,' 8 = Alaska incluing Aleutian Islands'
      PRINT *,' 9 = West Coast - North and Central America'
      PRINT *,' 10 = West Coast - South America'
      READ (UNIT=*,FMT=*,END=8000,ERR=8000)NREG
8000  IF ((NREG .LT. 0) .OR. (NREG .GT. 10)) THEN
         PRINT *,' Please input a number between 0 and 10 inclusive'
         GOTO 8000
      ENDIF
      PRINT *,' Which colour table do you wish to use'
      PRINT *,' HLS = 1'
      PRINT *,' CMY = 2'
      PRINT *,' RGB = 3'
500   READ (UNIT=*,FMT=*,END=600,ERR=600) ICOL
600   IF ((ICOL.LT.1) .OR. (ICOL.GT. 3)) THEN
         PRINT *,' Please input a number in the range 1 to 3'
         GOTO 500
      ENDIF
C
C
C     Initiate BIZMAP
C
C
C     Select a device. This is being left as blank to force
C     a dialogue so that I can run the program on
C     Tektronix 4010 - IBM running suitable emulator
C     DEC VT 340     - available in the Computer Centre
C     Dummy device   - any non graphics device, e.g.
C                      IBM PS/2 running Kermit (DEC VT1xx)
C
C
      PRINT *,' Select device, a list of valid devices maybe'
      PRINT *,' obtained by typing'
      PRINT *,' list *'
      PRINT *,' at the GROUTE prompt'
      CALL GROUTE(' ')
      CALL GOPEN
      CALL GSEGCR(1)
      CALL GSURFE
      IF     (IRES .EQ. 1) THEN
         CALL WEXTND
      ELSEIF (IRES .EQ. 2) THEN
         CALL WRED1
      ELSEIF (IRES .EQ. 3) THEN
         CALL WRED2
      ELSEIF (IRES .EQ. 4) THEN
         CALL WRED3
      ELSEIF (IRES .EQ. 5) THEN
         CALL WRED4
      ENDIF
      NR=7
      KOLOR=0
      CALL WOPEN(0,NR)
      CALL WPROJ(IPROJ)
      CALL WCENTR(LONG,LAT)
      CALL WDEFC(KOLOR)
      IF     (ICOL .EQ.2 ) THEN
         CALL RCMODE('CMY',100)
      ELSEIF (ICOL .EQ. 3) THEN
         CALL RCMODE('RGB',100)
      ENDIF
      CALL WPLOT('    ',0.0)
C
C
C     I have now got to convert the latitude and longitude
C     into the UNIRAS units of mm. This has to be done
C     *****     AFTER     *****
C     a call to WPLOT
C
C
      CALL CONVRT(TRIAL)
      CALL PLOTEM(TRIAL,NREG)
      CALL GSEGCR(1)
      CALL GCLOSE
      END
***********
*
*
      SUBROUTINE DATAIN(TRIAL)
*
*
***********
C
C
C     This subroutine reads in the tsunami data
C
C
C     UNIRAS uses the following unit numbers
C     5,6,7,20,21,22,24,25,26,27,28,33
C     So I have used 50.
C
      LOGICAL TRIAL
      CHARACTER*80 FILNAM
      COMMON /TSUNAM/
     +        REG0LA( 378) , REG0LO( 378) ,
     +        REG1LA( 206) , REG1LO( 206) ,
     +        REG2LA(  41) , REG2LO(  41) ,
     +        REG3LA(  54) , REG3LO(  54) ,
     +        REG4LA(  60) , REG4LO(  60) ,
     +        REG5LA(1540) , REG5LO(1540) ,
     +        REG6LA(  80) , REG6LO(  80) ,
     +        REG7LA( 144) , REG7LO( 144) ,
     +        REG8LA( 245) , REG8LO( 245) ,
     +        REG9LA( 285) , REG9LO( 285)
      IF (TRIAL .EQ. .TRUE.) THEN
         PRINT *,' Entering data input phase'
      ENDIF
      FILNAM='tsunami.dat'
      OPEN (UNIT=50,FILE=FILNAM,ERR=30,STATUS='OLD')
      GOTO 40
30      PRINT *,' Error opening data file'
        PRINT *,' Program terminates'
        STOP
40    DO 100 I=1,378
100     READ(UNIT=50,FMT=1000)REG0LA(I),REG0LO(I)
1000  FORMAT(1X,F7.2,2X,F7.2)
      DO 110 I=1,206
110     READ(UNIT=50,FMT=1000)REG1LA(I),REG1LO(I)
      DO 120 I=1,41
120     READ(UNIT=50,FMT=1000)REG2LA(I),REG2LO(I)
      DO 130 I=1,54
130     READ(UNIT=50,FMT=1000)REG3LA(I),REG3LO(I)
      DO 140 I=1,60
140     READ(UNIT=50,FMT=1000)REG4LA(I),REG4LO(I)
      DO 150 I=1,1540
150     READ(UNIT=50,FMT=1000)REG5LA(I),REG5LO(I)
      DO 160 I=1,80
160     READ(UNIT=50,FMT=1000)REG6LA(I),REG6LO(I)
      DO 170 I=1,144
170     READ(UNIT=50,FMT=1000)REG7LA(I),REG7LO(I)
      DO 180 I=1,245
180     READ(UNIT=50,FMT=1000)REG8LA(I),REG8LO(I)
      DO 190 I=1,285
190     READ(UNIT=50,FMT=1000)REG9LA(I),REG9LO(I)
      IF (TRIAL .EQ. .TRUE.) THEN
         DO 200 I=1,10
200      PRINT *,REG0LA(I),'   ',REG0LO(I)
         PRINT *,' Exiting data input phase'
         READ *,DUMMY
      ENDIF
      END
**********
*
*
      SUBROUTINE CONVRT(TRIAL)
*
*
**********
C
C
C     I have to call the conversion routine once per data value
C     This means 3942 calls to WGETMM
C
C
      LOGICAL TRIAL
      COMMON /TSUNAM/
     +        REG0LA( 378) , REG0LO( 378) ,
     +        REG1LA( 206) , REG1LO( 206) ,
     +        REG2LA(  41) , REG2LO(  41) ,
     +        REG3LA(  54) , REG3LO(  54) ,
     +        REG4LA(  60) , REG4LO(  60) ,
     +        REG5LA(1540) , REG5LO(1540) ,
     +        REG6LA(  80) , REG6LO(  80) ,
     +        REG7LA( 144) , REG7LO( 144) ,
     +        REG8LA( 245) , REG8LO( 245) ,
     +        REG9LA( 285) , REG9LO( 285)
      COMMON /MMTSUN/
     +        MM0LA( 378) , MM0LO( 378) ,
     +        MM1LA( 206) , MM1LO( 206) ,
     +        MM2LA(  41) , MM2LO(  41) ,
     +        MM3LA(  54) , MM3LO(  54) ,
     +        MM4LA(  60) , MM4LO(  60) ,
     +        MM5LA(1540) , MM5LO(1540) ,
     +        MM6LA(  80) , MM6LO(  80) ,
     +        MM7LA( 144) , MM7LO( 144) ,
     +        MM8LA( 245) , MM8LO( 245) ,
     +        MM9LA( 285) , MM9LO( 285)
      IF (TRIAL .EQ. .TRUE.) THEN
         PRINT *,' Entering convert'
      ENDIF
      DO 100 I=1,378
100   CALL WGETMM(REG0LO(I),REG0LA(I),MM0LO(I),MM0LA(I))
      DO 110 I=1,206
110   CALL WGETMM(REG1LO(I),REG1LA(I),MM1LO(I),MM1LA(I))
      DO 120 I=1,41
120   CALL WGETMM(REG2LO(I),REG2LA(I),MM2LO(I),MM2LA(I))
      DO 130 I=1,54
130   CALL WGETMM(REG3LO(I),REG3LA(I),MM3LO(I),MM3LA(I))
      DO 140 I=1,60
140   CALL WGETMM(REG4LO(I),REG4LA(I),MM4LO(I),MM4LA(I))
      DO 150 I=1,1540
150   CALL WGETMM(REG5LO(I),REG5LA(I),MM5LO(I),MM5LA(I))
      DO 160 I=1,80
160   CALL WGETMM(REG6LO(I),REG6LA(I),MM6LO(I),MM6LA(I))
      DO 170 I=1,144
170   CALL WGETMM(REG7LO(I),REG7LA(I),MM7LO(I),MM7LA(I))
      DO 180 I=1,245
180   CALL WGETMM(REG8LO(I),REG8LA(I),MM8LO(I),MM8LA(I))
      DO 190 I=1,285
190   CALL WGETMM(REG9LO(I),REG9LA(I),MM9LO(I),MM9LA(I))
      IF (TRIAL .EQ. .TRUE.) THEN
         PRINT *,' Exiting convert'
      ENDIF
      END
**********
*
*
      SUBROUTINE PLOTEM(TRIAL,NREG)
*
*
**********
C
C
C     This subroutine plots all of the tsunamis
C     onto the map as coloured points, with a
C     different colour per region. I have chosen
C     a dot size of 1 mm, and step through the
C     colour pallette. I hope I have enough colours
C     If not .....
C
C
      LOGICAL TRIAL
      INTEGER NREG
      COMMON /MMTSUN/
     +        MM0LA( 378) , MM0LO( 378) ,
     +        MM1LA( 206) , MM1LO( 206) ,
     +        MM2LA(  41) , MM2LO(  41) ,
     +        MM3LA(  54) , MM3LO(  54) ,
     +        MM4LA(  60) , MM4LO(  60) ,
     +        MM5LA(1540) , MM5LO(1540) ,
     +        MM6LA(  80) , MM6LO(  80) ,
     +        MM7LA( 144) , MM7LO( 144) ,
     +        MM8LA( 245) , MM8LO( 245) ,
     +        MM9LA( 285) , MM9LO( 285)
      DATA DWIDTH/1.0/
      IF (TRIAL .EQ. .TRUE.) THEN
         DWIDTH=5.0
         PRINT *,' Entering Plot points'
      ENDIF
      IF (NREG .EQ.0) THEN
         KOLOUR=2
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM0LO,MM0LA, 378)
         KOLOUR=3
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM1LO,MM1LA, 206)
         KOLOUR=4
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM2LO,MM2LA,  41)
         KOLOUR=5
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM3LO,MM3LA,  54)
         KOLOUR=6
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM4LO,MM4LA,  60)
         KOLOUR=7
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM5LO,MM5LA,1540)
         KOLOUR=0
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM6LO,MM6LA,  80)
         KOLOUR=24
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM7LO,MM7LA, 144)
         KOLOUR=23
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM8LO,MM8LA, 245)
         KOLOUR=22
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM9LO,MM9LA, 285)
      ELSEIF (NREG .EQ. 1) THEN
         KOLOUR=0
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM0LO,MM0LA, 378)
      ELSEIF (NREG .EQ. 2) THEN
         KOLOUR=2
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM1LO,MM1LA, 206)
      ELSEIF (NREG .EQ. 3) THEN
         KOLOUR=12
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM2LO,MM2LA,  41)
      ELSEIF (NREG .EQ. 4) THEN
         KOLOUR=4
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM3LO,MM3LA,  54)
      ELSEIF (NREG .EQ. 5) THEN
         KOLOUR=5
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM4LO,MM4LA,  60)
      ELSEIF (NREG .EQ. 6) THEN
         KOLOUR=6
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM5LO,MM5LA,1540)
      ELSEIF (NREG .EQ. 7) THEN
         KOLOUR=7
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM6LO,MM6LA,  80)
      ELSEIF (NREG .EQ. 8) THEN
         KOLOUR=8
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM7LO,MM7LA, 144)
      ELSEIF (NREG .EQ. 9) THEN
         KOLOUR=9
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM8LO,MM8LA, 245)
      ELSEIF (NREG .EQ. 10) THEN
         KOLOUR=10
         CALL GWICOL(DWIDTH,KOLOUR)
         CALL GDOT(MM9LO,MM9LA, 285)
      ENDIF
      IF (TRIAL .EQ. .TRUE.) THEN
         PRINT *,' Exiting Plot points'
      ENDIF
      END