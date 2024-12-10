      PROGRAM CH1102
      IMPLICIT LOGICAL (A-Z)
      INTEGER N
      PARAMETER (N = 10)
      REAL H,W,BMI
      DIMENSION H(N)
      DIMENSION W(N)
      DIMENSION BMI(N)
      INTEGER I

        DO 10 I = 1, N
          READ 100, H(I), W(I)
 100      FORMAT (F4.2,1X,F3.0)
          BMI(I) = W(I)/(H(I)*H(I))
 10     CONTINUE

        DO 20 I = 1, N
          PRINT 200, BMI(I)
 200     FORMAT (2X,F5.0)
 20     CONTINUE

      END PROGRAM CH1102