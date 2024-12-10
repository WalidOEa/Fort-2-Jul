      PROGRAM CH0909
      REAL X
      DIMENSION X(100)
      REAL SUM
      INTEGER I, N
      OPEN (UNIT=1,FILE='DATA.TXT')
      SUM = 0.0
      READ (UNIT=1,FMT=*)N
      DO 100 I=1,N
        READ (UNIT=1,FMT=*) X(I)
        SUM = SUM + X(I)
100   CONTINUE
      PRINT 200,N
200   FORMAT(I3,' VALUES ARE:')
      DO 300 I=1,N
         PRINT 400,X(I)
400      FORMAT(1X,E10.3)
300   CONTINUE
      PRINT *,'SUM OF ',N,' VALUES IS ',SUM
      CLOSE(1)
      END