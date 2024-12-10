      PROGRAM CH0904
      IMPLICIT LOGICAL (A-Z)
      INTEGER I
      REAL SMALL
      REAL BIG
        SMALL=1.0
        BIG=1.0
        DO 10 I = 1, 50
          PRINT 100, I, SMALL, BIG
 100      FORMAT (' ',I3,' ',F7.3,' ',F7.3)
          SMALL = SMALL/10.0
          BIG = BIG*10.0
 10     CONTINUE
      END PROGRAM CH0904