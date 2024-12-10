      PROGRAM CH1302
      IMPLICIT LOGICAL (A-Z)
      INTEGER YEAR, N, MONTH, DAY, T

C CALCULATES DAY AND MONTH FROM YEAR AND
C DAY-WITHIN-YEAR
C T IS AN OFFSET TO ACCOUNT FOR LEAP YEARS.
C NOTE THAT THE FIRST CRITERIA IS DIVISION BY 4
C BUT THAT CENTURIES ARE ONLY
C LEAP YEARS IF DIVISIBLE BY 400
C NOT 100 (4 * 25) ALONE.

        PRINT *, ' YEAR, FOLLOWED BY DAY WITHIN YEAR'
        READ *, YEAR, N
C CHECKING FOR LEAP YEARS
        IF ((YEAR/4)*4.EQ.YEAR) THEN
          T = 1
          IF ((YEAR/400)*400.EQ.YEAR) THEN
            T = 1
          ELSE IF ((YEAR/100)*100.EQ.YEAR) THEN
            T = 0
          END IF
        ELSE
          T = 0
        END IF
C ACCOUNTING FOR FEBRUARY
        IF (N.GT.(59+T)) THEN
          DAY = N + 2 - T
        ELSE
          DAY = N
        END IF
        MONTH = (DAY+91)*100/3055
        DAY = (DAY+91) - (MONTH*3055)/100
        MONTH = MONTH - 2
        PRINT *, ' CALENDAR DATE IS ', DAY, MONTH, YEAR
      END PROGRAM CH1302