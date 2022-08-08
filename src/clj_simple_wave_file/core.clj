(ns clj-simple-wave-file.core
  (:require [clojure.java.io :as io])
  (:import [javax.sound.sampled
            AudioFileFormat$Type
            AudioFormat
            AudioInputStream
            AudioSystem
            Clip
            Line$Info
            LineEvent$Type
            LineListener])
  (:gen-class))

(def sample-rate 44100)

(defmacro shortify
  "Takes a floating-point number f in the range [-1.0, 1.0] and scales
  it to the range of a signed 16-bit integer. Clamps any overflows."
  [f]
  (let [max-short-as-double (double Short/MAX_VALUE)]
    `(let [clamped# (-> ~f (min 1.0) (max -1.0))]
       (short (* ~max-short-as-double clamped#)))))

(defn- make-stream
  [fa]
  (let [cursor* (atom 0)]
    (proxy [java.io.InputStream] []
      (read ^int
        ([^bytes buf off len]
         (when-not (zero? off) (throw (Exception. "NON-ZERO OFFSET ENCOUNTERED")))
         (when-not (zero? (mod len 2)) (throw (Exception. "ODD LEN ENCOUNTERED")))
         (dotimes [i (quot len 2)]
           (let [s (shortify (aget fa @cursor*))]
             (swap! cursor* inc)
             (aset-byte buf (* 2 i) (unchecked-byte (bit-shift-right s 8)))
             (aset-byte buf (inc (* 2 i)) (unchecked-byte (bit-and s 0xFF)))))
         len)))))

(defn save
  "Expects a file path and an array of floats in the range [-1.0, 1.0]."
  [path fa]
  (let [stream (make-stream fa)
        bit-depth 16
        channel-count 1
        signed true
        big-endian true
        audio-format (AudioFormat. sample-rate bit-depth channel-count signed big-endian)
        length (count fa) ;; length in samples
        type-wav AudioFileFormat$Type/WAVE]
    (io/make-parents path)
    (AudioSystem/write (AudioInputStream. stream audio-format length) type-wav (io/file path))))

(defn- ms->sample-n
  [ms]
  (int (* sample-rate (/ ms 1000.0))))

(defn -main
  [& args]
  (let [attack-ms 5
        decay-ms 300
        attack-sample-n (ms->sample-n attack-ms)
        decay-sample-n (ms->sample-n decay-ms)
        sample-n (+ attack-sample-n decay-sample-n)
        fa (float-array sample-n)
        frequency 440
        samples-per-cycle (/ sample-rate frequency)]
    (dotimes [i sample-n]
      (let [v (if (< (mod i samples-per-cycle)
                     (/ samples-per-cycle 2))
                1.0
                -1.0)]
        (let [p (if (<= i attack-sample-n)
                  (/ (double i) attack-sample-n)
                  (- 1 (/ (double (- i attack-sample-n))
                          decay-sample-n)))]
          (aset fa i (* p v)))))
    (save "beep.wav" fa)))
