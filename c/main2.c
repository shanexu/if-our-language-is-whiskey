#include <stdio.h>

int even(int n);
int odd(int n);

int even(int n) {
  if (n == 0) {
    return 1;
  }
LBB0_2:
  if (n == 1) {
    return 0;
  }
  n-=2;
  if (n != 0) {
    goto LBB0_2;
  }
  return 1;
}

int odd(int n) {
  if (n == 0) {
    return 0;
  }
  return even(n - 1);
}

int main() {
  printf("%d\n", even(1000000));
}
