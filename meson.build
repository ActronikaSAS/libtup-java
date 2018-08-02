project('libtup-java', ['cpp', 'java'],
  version : '0.1',
  meson_version : '>= 0.43.0',
  default_options : ['warning_level=2'])

compiler = meson.get_compiler('cpp')

warning_flags = [
    '-Wno-unused-parameter',
    '-Wformat-nonliteral',
    '-Wformat-security',
    '-Winit-self',
    '-Wswitch-default',
    '-Wuninitialized',
    '-Wmaybe-uninitialized',
    '-Wshadow',
    '-Wframe-larger-than=4096',
    '-Wpointer-arith',
    '-Wundef',
    '-Wcast-qual',
    '-Wno-type-limits'
    ]

add_project_arguments(compiler.get_supported_arguments(warning_flags),
    language: 'cpp')

local_src = [ 'jni/tup-jni.cpp' ]

local_cxxargs = []

# check for JNI
found = compiler.has_header('jni.h')
if not found
  # check for some non standard path (taken from CMake FindJNI)
  dirs = [ '/usr/include',
      '/usr/local/include',
      '/usr/lib/java/include',
      '/usr/local/lib/java/include',
      '/usr/lib/jvm/java/include',
      '/usr/lib/jvm/java-6-sun/include',
      '/usr/lib/jvm/java-1.5.0-sun/include',
      '/usr/lib/jvm/java-6-openjdk/include',
      '/usr/local/share/java/include',
      '/usr/lib/j2sdk1.4-sun/include',
      '/usr/lib/j2sdk1.5-sun/include',
      '/opt/sun-jdk-1.5.0.04/include',
      # Debian specific path for default JVM
      '/usr/lib/jvm/default-java/include',
      ]

  foreach idir: dirs
    inc_arg = '-I' + idir
    if not found and compiler.has_header('jni.h', args: inc_arg)
      local_cxxargs += [inc_arg]

      if target_machine.system() == 'linux'
        local_cxxargs += [inc_arg + '/linux']
      endif

      found = true
    endif
  endforeach
endif

if not found
  error('`jni.h` not found')
endif

libtup_dep = dependency('libtup', fallback: ['libtup', 'libtup_dep'])

jnilib = shared_library('tup-jni', local_src,
    cpp_args: local_cxxargs,
    dependencies: [libtup_dep])

subdir('java')
