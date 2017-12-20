#pragma once
#include <opencv2/opencv.hpp>
#include <stdlib.h>
#include <math.h>
#include <vector>
using namespace std;

class BiCEDescriptor {
private:
	double csigma = 1;
	double ssigma = 2;
	double radius = 6;
	double eps = 4;
	uchar *I;
	double *g;
	double *angle;
	double ***H;
public:
	vector<int> Bin;
	int rows, cols;
	int bx;
	int by;
	int b0;
	int nx;
	int ny; 
	int n0;
public:
	BiCEDescriptor();
	BiCEDescriptor(cv::Mat patch, int bx, int by, int b0, int nx, int ny, int n0);
	double Distance(cv::Point a, cv::Point b);
	double Angle(double gy, double gx);
	double GaussianWeighted(double x, double sigma);
	void GradientMagnitudeNormalization();
	void EdgeAggregation();
	void Binarize();
	void RemoveAll();
};