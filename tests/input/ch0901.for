      PROGRAM CH0901
      IMPLICIT LOGICAL (A-Z)
      INTEGER T

        PRINT *, ' '
        PRINT *, ' TWELVE TIMES TABLE'
        PRINT *, ' '
        DO 10 T = 1, 12
          PRINT 100, T, T*12
 100      FORMAT (' ',I3,' * 12 = ',I3)
 10     CONTINUE
      END PROGRAM CH0901