      PROGRAM CH0905
      IMPLICIT LOGICAL (A-Z)
      INTEGER I
      REAL SMALL
      REAL BIG
        BIG=1.0
        SMALL=1.0
        DO 10 I = 1, 50
          PRINT 100, I, SMALL, BIG
 100      FORMAT (' ',I3,' ',E10.4,' ',E10.4)
          SMALL = SMALL/10.0
          BIG = BIG*10.0
 10     CONTINUE
      END PROGRAM CH0905