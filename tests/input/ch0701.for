      PROGRAM RAIN
      IMPLICIT LOGICAL (A-Z)
      REAL RAINFL, SUM, AVERGE
      DIMENSION RAINFL(12)
      INTEGER MONTH
        PRINT *,' Type in the rainfall values'
        PRINT *,' one per line'
        DO 10 MONTH=1,12
         READ *, RAINFL(MONTH)
10      CONTINUE
        SUM = 0.0
        DO 20 MONTH=1,12
          SUM = SUM + RAINFL(MONTH)
20      CONTINUE
        AVERGE = SUM / 12
        PRINT *,' Average monthly rainfall was'
        PRINT *, AVERGE
       END