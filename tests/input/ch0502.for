      PROGRAM CH0502
C
C THIS PROGRAM READS IN THREE NUMBERS AND SUMS
C AND AVERAGES THEM.
C
      IMPLICIT LOGICAL (A-Z)
      REAL NUMBR1,NUMBR2,NUMBR3,AVRAGE,TOTAL
      INTEGER N
        N = 3
        TOTAL = 0.0
        PRINT *,'TYPE IN THREE NUMBERS'
        PRINT *,'SEPARATED BY SPACES OR COMMAS'
        READ *,NUMBR1,NUMBR2,NUMBR3
        TOTAL= NUMBR1+NUMBR2+NUMBR3
        AVRAGE=TOTAL/N
        PRINT *,'TOTAL OF NUMBERS IS',TOTAL
        PRINT *,'AVERAGE OF THE NUMBERS IS',AVRAGE
      END