#include "BiCEDescriptor.h"

BiCEDescriptor::BiCEDescriptor()
{
}

BiCEDescriptor::BiCEDescriptor(cv::Mat patch, int bx, int by, int b0, int nx, int ny, int n0)
{
	this->rows = patch.rows;
	this->cols = patch.cols;
	this->bx = bx;
	this->by = by;
	this->b0 = b0;
	this->nx = nx;
	this->ny = ny;
	this->n0 = n0;

	cv::GaussianBlur(patch, patch, cv::Size(3, 3), csigma, csigma);
	I = new uchar[rows * cols];

	for (int i = 0; i < rows; i++)
		for (int j = 0; j < cols; j++) {
			cv::Scalar intensity = patch.at<uchar>(i, j);
			I[i*cols + j] = intensity[0];
		}

	GradientMagnitudeNormalization();
	EdgeAggregation();
	Binarize();
	RemoveAll();
}

double BiCEDescriptor::Distance(cv::Point a, cv::Point b)
{
	return sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
}

double BiCEDescriptor::Angle(double gy, double gx)
{
	if (gy >= 0) return atan2(gy, gx);
	else return atan2(gy, gx) + 2.0 * CV_PI;
}

double BiCEDescriptor::GaussianWeighted(double x, double sigma)
{
	double pi = CV_PI;
	return exp(-((x/sigma)*(x/sigma)/2))/(sigma*sqrt(2.0*pi));
}

void BiCEDescriptor::GradientMagnitudeNormalization()
{

	g = new double[rows * cols];
	angle = new double[rows * cols];
	for (int i = 0; i < rows; i++) {
		for (int j = 0; j < cols; j++) {
			double gx, gy;
			if (i == rows - 1) gx = 0;
			else gx = I[(i + 1)*cols + j] - I[i*cols + j];
			if (j == cols - 1) gy = 0;
			else gy = I[i*cols + j + 1] - I[i*cols + j];
			g[i*cols + j] = sqrt(gx*gx + gy*gy);
			angle[i*cols + j] = Angle(gy, gx);
		}
	}

	double *tmp = new double[rows * cols];
	for (int i = 0; i < rows; i++) {
		for (int j = 0; j < cols; j++) {
			double avg = 0;
			tmp[i*cols + j] = g[i*cols + j];
			for (int k = max(i - radius, 0.0); k < min(i + radius, 1.0 * rows); k++) {
				for (int h = max(j - radius, 0.0); h < min(j + radius, 1.0 * cols); h++) {
					double dist = Distance(cv::Point(i, j), cv::Point(k, h));
					if (dist <= radius) {
						avg += g[k*cols + h] * GaussianWeighted(dist, ssigma);
					}
				}
			}
			tmp[i*cols + j] /= max(avg, eps);
		}
	}
	delete[] g;
	g = tmp;
}

void BiCEDescriptor::EdgeAggregation()
{
	double bx_size = sqrt(1.0 * rows * rows + 1.0 * cols * cols) / bx;
	double by_size = sqrt(1.0 * rows * rows + 1.0 * cols * cols) / by;
	double b0_size = CV_PI * 2 / b0;
	
	H = new double**[bx];
	for (int i = 0; i < bx; i++) {
		H[i] = new double*[by];
		for (int j = 0; j < by; j++) {
			H[i][j] = new double[b0];
			for (int k = 0; k < b0; k++) H[i][j][k] = 0;
		}
	}
	int centx = (rows >> 1);
	int centy = (cols >> 1);
	for (int i = 0; i < rows; i++) {
		for (int j = 0; j < cols; j++) {
			double ang = angle[i*cols + j];
			double xp = cos(ang)*(i - centx) - sin(ang)*(j - centy);
			double yp = sin(ang)*(i - centx) + cos(ang)*(j - centy);
			int xpn = min(max((bx >> 1) + int(xp / bx_size) - (xp < 0), 0), bx - 1);
			int ypn = min(max((by >> 1) + int(yp / by_size) - (yp < 0), 0), by - 1);
			int angn = min(max(int(ang / b0_size), 0), b0 - 1);
			H[xpn][ypn][angn] = g[i*cols + j];
		}	
	}
}

void BiCEDescriptor::Binarize()
{
	int count = 0;
	int s = 0;
	for (int i = 0; i < b0; i++) {
		uchar* mat = new uchar[bx * by];
		for (int j = 0; j < bx; j++) {
			for (int k = 0; k < by; k++) {
				mat[j*by + k] = (H[j][k][i] >= 0.1);
			}
		}
		cv::Mat sp(bx, by, CV_8U, mat);
		cv::resize(sp, sp, cv::Size(ny, nx));
		for (int j = 0; j < nx; j++)
			for (int k = 0; k < ny; k++) {
				cv::Scalar intensity = sp.at<uchar>(j, k);
				int cc = intensity[0];
				s = (s << 1) + cc;
				count++;
				if (count == 18) {
					Bin.push_back(s);
					s = 0;
					count = 0;
				}
			}
		delete[] mat;
	}
	if (count > 0) {
		Bin.push_back(s);
	}
}

void BiCEDescriptor::RemoveAll()
{
	delete[] I;
	delete[] g;
	delete[] angle;
	for (int i = 0; i < bx; i++) {
		for (int j = 0; j < by; j++) delete[] H[i][j];
		delete[] H[i];
	}
	delete[] H;
}
