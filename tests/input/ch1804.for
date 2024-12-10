
      SUBROUTINE JOIN(A,M,B,N,C)
        REAL A,B,C
        INTEGER I,M,N
        DIMENSION A(M),B(N),C(*)
          DO 1 I=1,M
            C(I)=A(I)
  1       CONTINUE
          DO 2 I=1,N
            C(I+M)=B(I)
  2       CONTINUE
        END      
      
      PROGRAM C1804A
      REAL X,Y,Z
      INTEGER I,M,N
      DIMENSION X(10),Y(15),Z(25)
        PRINT*,'INPUT M (<= 10)'
        READ*,M
        PRINT*,'INPUT ',M,' VALUES OF X'
        READ*,(X(I),I=1,M)
        PRINT*,'INPUT N (<= 15)'
        READ*,N
        PRINT*,'INPUT ',N,' VALUES OF Y'
        READ*,(Y(I),I=1,N)
        CALL JOIN(X,M,Y,N,Z)
        PRINT*,'ARRAY Z:'
        DO 1 I=1,M+N
          PRINT*,Z(I)
1       CONTINUE
      END