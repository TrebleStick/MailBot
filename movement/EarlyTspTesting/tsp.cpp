// https://stackoverflow.com/questions/28702426/no-matching-function-for-call-to-mapflatfunctionname/29174891#29174891

#include <stdio.h>
#include <stdlib.h>
int queue[100], stack[100], alt[100], v[100];
int sp,head,tail,i,n,g,j,s,path,module,map[100][100];
int main()
{
  printf("Number of cities:");
  scanf( "%d",&n);
  printf("Max Segment:");
  scanf( "%d",&module);
  printf("Seed:");
  scanf( "%d",&g);
  srand(g);
// Generating the sysmetric connection matrix randomly
 for (i=0   ; i<n ; i++) {
    for (j=i+1 ; j<n ; j++) {
       map[i][j]= rand() % (module+1);
       map[j][i]=map[i][j];
      }
 for (j=0 ; j<n ; j++) printf("%3d ",map[i][j]);
 printf("\n");
  }
//Start with an initial solution from city 1
 for (i=0 ; i<n ; i++) {
    queue[i]=i;
  }
// Set route length to high value
   path=module*n;
   stack[0]=queue[0];
   alt[0]=0;
   printf("running...\n");
   sp=0;
   head=0;
   tail=n-1;
   s=0;
// Explore a branch of the factorial tree
   while(1) {
      while(sp<n-1 && s<path)  {
          sp++;
          head++; if (head==n) head=0;
          stack[sp]=queue[head];
          s=s+map[stack[sp]][stack[sp-1]];
          alt[sp]=n-sp-1;
       }
// Save a better solution
      if (s+map[stack[sp]][stack[0]]<path) {
        path=s+map[stack[sp]][stack[0]];
        for (i=0 ; i<n ; i++) v[i]=stack[i]+1;
      }
// Leaving nodes when there is no more  branches
      while (alt[sp]==0 && sp>=0) {
        tail++; if (tail==n) tail=0;
        queue[tail]=stack[sp];
        s=s-map[stack[sp]][stack[sp-1]];
        sp--;
      }
// If Bottom of stack is reached then stop
      if (sp<0) break;
      tail++; if (tail==n) tail=0;
      queue[tail]=stack[sp];
      s=s-map[stack[sp]][stack[sp-1]];
// Explore an alternate branch
      alt[sp]=alt[sp]-1;
      head++; if (head==n) head=0;
      stack[sp]=queue[head];
      s=s+map[stack[sp]][stack[sp-1]];
  }
  printf("best route=%d\n",path);
  for (i=0 ; i<n ; i++) printf("%d ",v[i]);
  printf("%d\n",stack[0]+1);
  return 0;
}
