#include <jni.h>
#include <string>
#include <fstream>
#include <time.h>
#include <stdio.h>
#include <opencv2/core.hpp>
#include "BiCEDescriptor.h"
#include <android/log.h>
#include "clone.h"
using namespace std;
using namespace cv;

struct patch_info{
    string image_name;
    int index;
    int patchx, patchy;
    BiCEDescriptor bice;
};
static int num_images;
static vector<patch_info> dataset;
static int cb[1200000];

extern "C" {
JNIEXPORT jstring JNICALL
Java_com_tuanbi97_opencvtest_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_tuanbi97_opencvtest_MainActivity_computeBiCE(JNIEnv *env, jobject, jlong matPtr){
    Mat mat = *(Mat *) matPtr;
    resize(mat, mat, Size(60, 60));
    double duration;

    clock_t start = clock();
    BiCEDescriptor bice(mat, 32, 32, 4, 6, 18, 4);
    duration = (1.0*clock() - 1.0*start) / CLOCKS_PER_SEC;
    char *text = new char[20];
    sprintf(text, "%d %d %d %f", bice.rows, bice.cols, bice.Bin.size(), duration);
    string str(text);
    return env->NewStringUTF(str.c_str());
};

JNIEXPORT void JNICALL
Java_com_tuanbi97_opencvtest_MainActivity_loadDataset(JNIEnv *env, jobject){

    dataset.clear();
    for (int i = 1; i < (1 << 18); i++){
        for (int j = 0; j < 18; j++){
            cb[i] += ((i >> j) & 1);
        }
    }
    std::ios::sync_with_stdio(false);
    ifstream file;
    file.open("/storage/emulated/0/sketchdata/dataset.txt");
    if (file.is_open()){
        int num_patch, bsize;
        file >> num_images >> num_patch >> bsize;
        for (int i = 0; i < num_images; i++){
            string image_name;
            file.get();
            file >> image_name;
            for (int j = 0; j < num_patch; j++){
                patch_info pi;
                pi.image_name = image_name;
                pi.index = i;
                file >> pi.patchx >> pi.patchy;
                for (int k = 0; k < bsize; k++){
                    int binval;
                    file >> binval;
                    pi.bice.Bin.push_back(binval);
                }
                dataset.push_back(pi);
            }
        }
    }
}

double JaccardSimilarity(vector<int> &bin1, vector<int> &bin2) {
    double eps = 0.1;
    double Intersect = 0;
    double Union = 0;
    for (int i = 0; i < bin1.size(); i++) {
        Intersect += cb[bin1[i] & bin2[i]];
        Union += cb[bin1[i] | bin2[i]];
    }
    return Intersect / (Union + eps);
}

JNIEXPORT jdoubleArray JNICALL
Java_com_tuanbi97_opencvtest_MainActivity_findMatch(JNIEnv *env, jobject, jlong matAddr, int patch_size, double overlap, int bx, int by, int b0, int nx, int ny, int n0){
    Mat im = *(Mat*) matAddr;
    __android_log_print(ANDROID_LOG_INFO, "matsize:", "%d %d", im.rows, im.cols);
    int step = patch_size * (1 - overlap);
    double *score = new double[num_images];
    for (int i = 0; i < num_images; i++) score[i] = 0;
    int dem = 0;
    for (int x = 0; x < im.rows - patch_size + 1; x += step) {
        for (int y = 0; y < im.cols - patch_size + 1; y += step) {
            cv::Mat patch = cv::Mat(im, cv::Rect(y, x, patch_size, patch_size)).clone();
            BiCEDescriptor bice(patch, bx, by, b0, nx, ny, n0);
            for (int i = 0; i < dataset.size(); i++) {
                patch_info p = dataset[i];
                double js = JaccardSimilarity(p.bice.Bin, bice.Bin);
                if (js > 0.2)
                    score[p.index] += js;
            }
            dem++;
        }
    }

    jdoubleArray ret = env->NewDoubleArray(num_images);
    env->SetDoubleArrayRegion(ret, 0, num_images, score);
    return ret;
}

JNIEXPORT jdoubleArray JNICALL
Java_com_tuanbi97_opencvtest_SketchView_00024matchingTask_findPartMatch(JNIEnv *env, jobject, jlong matAddr, int Xmin, int Ymin, int Xmax, int Ymax, int patch_size, double overlap, int bx, int by, int b0, int nx, int ny, int n0){
    //__android_log_print(ANDROID_LOG_INFO, "partmatch", "%d %d %d %d", Xmin, Ymin, Xmax, Ymax);
    Mat im = *(Mat*) matAddr;
    int step = patch_size * (1 - overlap);
    double *score = new double[num_images];
    for (int i = 0; i < num_images; i++) score[i] = 0;
    int dem = 0;
    //__android_log_print(ANDROID_LOG_INFO, "partmatch", "%d %d %d %d", max(0, ((int) (Xmin/step)) * step - 3*step), max(0, ((int) (Ymin/step)) * step - 3*step), Xmax, Ymax);
    for (int y = max(0, ((int) (Ymin/step)) * step - 3*step); y <= Ymax && y + patch_size < im.rows; y += step) {
        for (int x = max(0, ((int) (Xmin/step)) * step - 3*step); x <= Xmax && x + patch_size < im.cols; x += step) {
            cv::Mat patch = cv::Mat(im, cv::Rect(x, y, patch_size, patch_size)).clone();
            BiCEDescriptor bice(patch, bx, by, b0, nx, ny, n0);
            for (int i = 0; i < dataset.size(); i++) {
                patch_info p = dataset[i];
                double js = JaccardSimilarity(p.bice.Bin, bice.Bin);
                if (js > 0.2)
                    score[p.index] += js;
            }
            dem++;
        }
    }

    jdoubleArray ret = env->NewDoubleArray(num_images);
    env->SetDoubleArrayRegion(ret, 0, num_images, score);
    return ret;
}

JNIEXPORT void JNICALL
Java_com_tuanbi97_opencvtest_MixActivity_00024BlendTask_blend(JNIEnv *env, jobject, jlong bgAddr, jlong fgAddr, jlong maskAddr, jlong retAddr, jint offsetx, jint offsety){
    Mat background = *(Mat*) bgAddr;
    Mat foreground = *(Mat*) fgAddr;
    Mat mask = *(Mat*) maskAddr;
    Mat& result = *(Mat*) retAddr;
    blend::seamlessClone(background, foreground, mask, offsetx, offsety, result, blend::CLONE_MIXED_GRADIENTS);
}

}
