/*
 * Copyright (C) 2008 Leandro A. F. Fernandes and Manuel M. Oliveira
 *
 * author   : Fernandes, Leandro A. F.
 * e-mail   : laffernandes@gmail.com
 * home page: http://www.inf.ufrgs.br/~laffernandes
 *
 *
 * The complete description of the implemented techinique can be found at
 *
 *      Leandro A. F. Fernandes, Manuel M. Oliveira
 *      Real-time line detection through an improved Hough transform voting scheme
 *      Pattern Recognition (PR), Elsevier, 41:1, 2008, 299-314.
 *      DOI: http://dx.doi.org/10.1016/j.patcog.2007.04.003
 *      Project Page: http://www.inf.ufrgs.br/~laffernandes/kht.html
 *
 * If you use this implementation, please reference the above paper.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */

#include "kht.h"
#include "linking.h"
#include "subdivision.h"
#include "voting.h"
#include "peak_detection.h"

// Kernel-based Hough transform (KHT) for detecting straight lines in images.
void
kht(lines_list_t &lines, section_list_t &sections, unsigned char *binary_image, const size_t image_width, const size_t image_height, const size_t cluster_min_size, const double cluster_min_deviation, const double delta, const double kernel_min_height, const double n_sigmas)
{
	static strings_list_t strings;
	static clusters_list_t clusters;
	static accumulator_t accumulator;

	// Group feature pixels from an input binary into clusters of approximately collinear pixels.
	find_strings( strings, binary_image, image_width, image_height, cluster_min_size );
	find_clusters( clusters, strings, cluster_min_deviation, cluster_min_size );

	// Perform the proposed Hough transform voting scheme.
	accumulator.init( image_width, image_height, delta );
	voting( accumulator, sections, clusters, kernel_min_height, n_sigmas );

	// Retrieve the most significant straight lines from the resulting voting map.
	peak_detection( lines, accumulator );
}
