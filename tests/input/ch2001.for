      BLOCK DATA MATHS
      COMMON /CONSTS/ C,E,PI
      DOUBLE PRECISION C,E,PI
      DATA C/299792458.0D0/
      DATA E/2.71828182845904523D0/
      DATA PI/3.14159265358979323D0/
      END

      SUBROUTINE SUB1(RADIUS,AREA,CIRCUM)
      IMPLICIT LOGICAL (A-Z)
      COMMON /CONSTS/ C,E,PI
      DOUBLE PRECISION C,E,PI
      DOUBLE PRECISION RADIUS
      DOUBLE PRECISION AREA, CIRCUM
        AREA = PI*RADIUS*RADIUS
        CIRCUM = 2.0D0*PI*RADIUS
      END SUBROUTINE SUB1

      PROGRAM CH2101
      IMPLICIT LOGICAL (A-Z)
      DOUBLE PRECISION R, A, C
      INTEGER I
        DO 10 I = 1, 5
          PRINT *, 'RADIUS?'
          READ *, R
          CALL SUB1(R,A,C)
          PRINT *, ' FOR RADIUS = ', R
          PRINT *, ' AREA = ', A
          PRINT *, ' CIRCUMFERENCE = ', C
 10     CONTINUE
      END PROGRAM CH2101