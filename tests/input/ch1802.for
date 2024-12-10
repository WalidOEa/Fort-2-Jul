      SUBROUTINE MINMAX(V,N,VMAX,VMIN)
      REAL V,VMIN,VMAX
      INTEGER I,N
      DIMENSION V(100)
       VMIN=V(1)
       VMAX=VMIN
       DO 1 I=2,N
        IF(V(I).GT.VMAX) THEN
          VMAX=V(I)
        ELSEIF(V(I).LT.VMIN) THEN
          VMIN=V(I)
        ENDIF
1      CONTINUE
      END
      PROGRAM C1802A
      REAL A,AMIN,AMAX
      INTEGER I,M
      DIMENSION A(100)
       PRINT*,'INPUT NUMBER OF NUMBERS (<= 100)'
       READ*,M
       PRINT *,'INPUT ',M,' NUMBERS'
       READ*,(A(I),I=1,M)
       CALL MINMAX(A,M,AMAX,AMIN)
       PRINT *,'MINIMUM = ',AMIN
       PRINT *,'MAXIMUM = ',AMAX
      END