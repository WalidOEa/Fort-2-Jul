      PROGRAM CH0908
      IMPLICIT LOGICAL (A-Z)
      CHARACTER*15 FNAME
      INTEGER AGE
      REAL WEIGHT
      CHARACTER*1 SEX

        PRINT *, ' TYPE IN YOUR FIRST NAME '
        READ *, FNAME
        PRINT *, ' TYPE IN YOUR AGE IN YEARS'
        READ *, AGE
        PRINT *, ' TYPE IN YOUR WEIGHT IN KILOS'
        READ *, WEIGHT
        PRINT *, ' TYPE IN YOUR SEX (F/M)'
        READ *, SEX
        PRINT *, ' YOUR PERSONAL DETAILS ARE'
        PRINT *
        PRINT 100
 100    FORMAT (4X,'FIRST NAME',4X,'AGE',1X,'WEIGHT',2X,'SEX')
        PRINT 200, FNAME, AGE, WEIGHT, SEX
 200    FORMAT (1X,A,2X,I3,2X,F5.2,2X,A)
      END PROGRAM CH0908