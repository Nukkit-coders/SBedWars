package net.mcpes.summit.hhm.bedwars.utils;

import java.io.*;

/**
 * @author hhm
 * @date 2017/7/13
 * @since SBedWars
 */

public class FileFunction {
    public static void copy(String in, String out) throws IOException {
        (new File(out)).mkdirs();
        // 获取源文件夹当前下的文件或目录
        File[] file = (new File(in)).listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                // 复制文件
                copyFile(file[i], new File(out + "/" + file[i].getName()));
            }
            if (file[i].isDirectory()) {
                // 复制目录
                String sourceDir = in + File.separator + file[i].getName();
                String targetDir = out + File.separator + file[i].getName();
                copyDirectory(sourceDir, targetDir);
            }
        }
    }

    // 复制文件
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        // 新建文件输入流并对它进行缓冲
        FileInputStream input = new FileInputStream(sourceFile);
        BufferedInputStream inBuff = new BufferedInputStream(input);
        // 新建文件输出流并对它进行缓冲
        FileOutputStream output = new FileOutputStream(targetFile);
        BufferedOutputStream outBuff = new BufferedOutputStream(output);
        // 缓冲数组
        byte[] b = new byte[1024 * 5];
        int len;
        while ((len = inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }
        // 刷新此缓冲的输出流
        outBuff.flush();
        //关闭流
        inBuff.close();
        outBuff.close();
        output.close();
        input.close();
    }

    // 复制文件夹
    public static void copyDirectory(String sourceDir, String targetDir) throws IOException {
        // 新建目标目录
        (new File(targetDir)).mkdirs();
        // 获取源文件夹当前下的文件或目录
        File[] file = (new File(sourceDir)).listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                // 源文件
                File sourceFile = file[i];
                // 目标文件
                File targetFile = new
                        File(new File(targetDir).getAbsolutePath()
                        + File.separator + file[i].getName());
                copyFile(sourceFile, targetFile);
            }
            if (file[i].isDirectory()) {
                // 准备复制的源文件夹
                String dir1 = sourceDir + "/" + file[i].getName();
                // 准备复制的目标文件夹
                String dir2 = targetDir + "/" + file[i].getName();
                copyDirectory(dir1, dir2);
            }
        }
    }

    public static void remove(File file) {
        if (file.isDirectory()) {
            removeFolder(file.getAbsolutePath());
        } else file.delete();
    }

    public static void removeFolder(String folderPath) {
        try {
            removeAllFile(folderPath); //删除完里面所有内容
            String FilePath = folderPath;
            FilePath = FilePath.toString();
            File myFilePath = new File(FilePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean removeAllFile(String path) {
        File File = new File(path);
        if (!File.exists()) {
            return false;
        }
        if (!File.isDirectory()) {
            return false;
        }
        String[] tempList = File.list();
        if (tempList == null) return false;
        File temp;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                removeAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                removeFolder(path + "/" + tempList[i]);//再删除空文件夹
                return true;
            }
        }
        return false;
    }
}
